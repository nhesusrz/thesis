/**
 * Copyright (C) 2014 Martín Pacheco.
 *
 * This file is part of my Thesis aplication called "Extraction and Analysis
 * System of Topics for Software History Reports". Faculty of Exact Sciences of
 * the UNICEN University. Tandil, Argentine. http://www.exa.unicen.edu.ar/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: mpacheco@alumnos.exa.unicen.edu.ar
 *
 * @author Martín Pacheco
 */
package algorithms;

import dao.DAOManager;
import dao.DAONameEnum;
import dto.BugDTO;
import dto.CommentDTO;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import logger.ThesisLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Parser extends DefaultHandler implements Runnable, Observer {

    AlgorithmManager parserController;
    Integer bugsParsed, commentsParsed;
    String fileName;
    BugDTO tmpBug;
    CommentDTO commentTmp;
    StringBuffer stringBufferTmp;
    SimpleDateFormat simpleDateFormat1, simpleDateFormat2;
    boolean resetDB;

    public Parser(String fileName, boolean resetDB, AlgorithmManager controllerParser) {
        this.parserController = controllerParser;
        bugsParsed = commentsParsed = 0;
        this.fileName = fileName;
        stringBufferTmp = new StringBuffer();
        simpleDateFormat1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US); // Sun, 13 Feb 2011 22:01:41 +0000
        simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US); // 2011-02-07T21:33:45.000Z
        this.resetDB = resetDB;
    }

    void destroy() {
        fileName = null;
        bugsParsed = null;
        commentsParsed = null;
        tmpBug = null;
        commentTmp = null;
        stringBufferTmp = null;
    }

    @Override
    public void run() {
        try {
            parseDocument();
        } catch (ParserConfigurationException e) {
            ThesisLogger.get().error("Parse.run: " + e.toString());
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Error1"));
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"));
        } catch (SAXException e) {
            ThesisLogger.get().error("Parse.run: " + e.toString());
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Error2"));
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"));
        } catch (IOException e) {
            ThesisLogger.get().error("Parse.run: " + e.toString());
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
            parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"));
        }
    }

    public boolean hasResults() {
        return bugsParsed > 0;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (atts.getLength() != 0) {
        }
        if (qName.equalsIgnoreCase("bug")) {
            tmpBug = new BugDTO();
        }
        if (qName.equalsIgnoreCase("comment")) {
            commentTmp = new CommentDTO();
        }
    }

    @Override
    public void characters(char[] buffer, int start, int length) throws SAXException {
        stringBufferTmp.append(new String(buffer, start, length));
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("bug")) {
            putBug(tmpBug);
        }
        if (qName.equalsIgnoreCase("bugid")) {
            tmpBug.setID(new Integer(stringBufferTmp.toString().trim()));
        }
        if (qName.equals("title")) {
            tmpBug.setTitle(replaceSpecialCharactersXML(stringBufferTmp.toString()).trim());
        }
        if (qName.equalsIgnoreCase("status")) {
            tmpBug.setStatus(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("owner")) {
            tmpBug.setOwner(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("closedOn")) {
            if (stringBufferTmp.toString().trim().equalsIgnoreCase("null")) {
                tmpBug.setClosedON(false);
                tmpBug.setClosedDate(null);
            } else {
                tmpBug.setClosedON(true);
                try {
                    Date dateTmp = simpleDateFormat2.parse(stringBufferTmp.toString().trim());
                    tmpBug.setClosedDate(dateTmp);
                } catch (ParseException e) {
                    ThesisLogger.get().error("ParseException: " + e.toString());
                }
            }
        }
        if (qName.equalsIgnoreCase("type")) {
            tmpBug.setType(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("priority")) {
            tmpBug.setPriority(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("component")) {
            tmpBug.setComponent(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("stars")) {
            tmpBug.setStars(new Integer(stringBufferTmp.toString().trim()));
        }
        if (qName.equalsIgnoreCase("reportedBy")) {
            tmpBug.setReportedBY(stringBufferTmp.toString().trim());
        }
        if (qName.equalsIgnoreCase("openedDate")) {
            try {
                Date dateTmp = simpleDateFormat1.parse(stringBufferTmp.toString().trim());
                tmpBug.setOpenedDate(dateTmp);
            } catch (ParseException e) {
                ThesisLogger.get().error("ParseException: " + e.toString());
            }
        }
        if (qName.equalsIgnoreCase("description")) {
            tmpBug.setDescription(replaceSpecialCharactersXML(stringBufferTmp.toString().trim()));
        }
        if (qName.equalsIgnoreCase("comment")) {
            tmpBug.addComment(commentTmp);
            commentsParsed++;
        }
        if (qName.equalsIgnoreCase("author")) {
            commentTmp.setAuthor(replaceSpecialCharactersXML(stringBufferTmp.toString().trim()));
        }
        if (qName.equalsIgnoreCase("when")) {
            try {
                Date dateTmp = simpleDateFormat1.parse(stringBufferTmp.toString().trim());
                commentTmp.setDate(dateTmp);
            } catch (ParseException e) {
                ThesisLogger.get().error("ParseException: " + e.toString());
            }
        }
        if (qName.equalsIgnoreCase("what")) {
            if (!stringBufferTmp.toString().equals("")) {
                commentTmp.setText(replaceSpecialCharactersXML(stringBufferTmp.toString().trim()));
            } else {
                commentTmp.setText("");
            }
        }
        stringBufferTmp = new StringBuffer();
    }

    private void parseDocument() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.Inic"));
        if (resetDB) {
            DAOManager.getInstance().deleteTablesForParsing();
        }
        long start = System.currentTimeMillis();
        parser.parse(fileName.replace("C:", "file:"), this); // Uses file: not c:
        long end = System.currentTimeMillis() - start;
        parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"));
        parserController.notify(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Parser.Mensage2"), bugsParsed, commentsParsed, end));
        bugsParsed = 0;
    }

    private synchronized void putBug(BugDTO tmpBug) {
        parserController.notify(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.Run"));
        bugsParsed++;
        DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).insert(tmpBug);
    }

    private String replaceSpecialCharactersXML(String source) {
        source = source.replaceAll("Q<E", "<");
        source = source.replaceAll("Q>E", ">");
        source = source.replaceAll("Q'E", "'");
        source = source.replaceAll("Q\"E", "\"");
        source = source.replaceAll("Q&E", "&");
        return source;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        parserController.notify((String) arg1);
    }
}
