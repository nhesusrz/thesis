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
package lucene;

import dao.DAOManager;
import dao.DAONameEnum;
import dao.DocumentDAO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;
import org.xml.sax.SAXException;
import util.Duration;
import util.ParametersEnum;
import util.PropertiesApp;

public class Indexer extends Observable implements Runnable {

    private IndexWriter index_writer;
    private int docsCount;
    private SimpleFSDirectory directory;

    private String stopWords;
    PorterStemmer stemmer = new PorterStemmer();

    public Indexer(Analyzer analyzer, File indexDir, File dataDir, boolean incremental) throws CorruptIndexException, LockObtainFailedException, IOException {
        index_writer = new IndexWriter(new SimpleFSDirectory(indexDir), analyzer, incremental, MaxFieldLength.UNLIMITED);
        docsCount = 0;
        loadStopWords();
    }

    public Indexer(Analyzer analyzer, File indexDir, boolean incremental) throws CorruptIndexException, LockObtainFailedException, IOException {
        directory = new SimpleFSDirectory(indexDir);
        if (IndexReader.indexExists(directory)) {
            index_writer = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
        } else {
            index_writer = new IndexWriter(directory, analyzer, incremental, MaxFieldLength.UNLIMITED);
        }
        index_writer.setUseCompoundFile(true);
        docsCount = 0;
        loadStopWords();
    }

    @Override
    public void run() {
        try {
            index();
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
        } catch (SAXException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error1"));
        }
    }

    public void destroy() throws CorruptIndexException, IOException {
        index_writer.close();
        index_writer = null;
    }

    public int getCountFiles() throws IOException {
        return index_writer.numDocs();
    }

    /**
     * Execute the index process.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SAXException
     */
    private void index() throws IOException, FileNotFoundException, SAXException {
        if (IndexReader.indexExists(directory)) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Mensage4"));
        }
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.Inic"));
        long start = System.currentTimeMillis();
        indexDocs();
        index_writer.optimize();
        index_writer.close();
        long end = System.currentTimeMillis();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.End"));
        setChanged();
        notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Mensage1"), docsCount, Duration.getDurationBreakdown(end - start)));
        docsCount = 0;
    }

    /**
     * Insert into lucene's index each document.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SAXException
     */
    private void indexDocs() throws IOException, FileNotFoundException, SAXException {
        docsCount = ((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount();
        for (int i = 1; i <= docsCount; i++) {
            dto.DocumentDTO docSource = (dto.DocumentDTO) ((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).get(i);
            Document docLucene = new Document();
            Field field = new Field(ParametersEnum.INDEX_FIELD1.toString(), Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.YES);
            docLucene.add(field);
            field = new Field(ParametersEnum.INDEX_FIELD2.toString(), DateTools.timeToString(docSource.getDate().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.YES);
            docLucene.add(field);
            field = new Field(ParametersEnum.INDEX_FIELD3.toString(), applyStemming(removeSimbols(docSource.getText())).toLowerCase(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
            //field.setBoost((float) 1.5);
            docLucene.add(field);
            index_writer.addDocument(docLucene);
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.Run"));
        }
    }

    /**
     * This remove some noice from the text documents.
     */
    private String removeSimbols(String text) {
        text = text.replace("(\r\n|\n\r|\r|\n)", " ");
        text = text.replace("_", " ");
        text = text.replace(".", " ");
        text = text.replace(":", " ");
        text = text.replace(";", " ");
        text = text.replace("/", " ");
        text = text.replace("[", " ");
        text = text.replace("]", " ");
        text = text.replace("{", " ");
        text = text.replace("}", " ");
        text = text.replace("@", " ");
        text = text.replace("#", " ");
        text = text.replace("*", " ");
        text = text.replace("&", " ");
        text = text.replace("%", " ");
        text = text.replace("~", " ");
        text = text.replace("·", " ");
        text = text.replace("º", " ");
        text = text.replace("ª", " ");
        text = text.replace("^", " ");
        text = text.replace("`", " ");
        text = text.replace("´", " ");
        text = text.replace("¨", " ");
        text = text.replace("<", " ");
        text = text.replace(">", " ");
        text = text.replace("|", " ");
        text = text.replace("(", " ");
        text = text.replace(")", " ");
        text = text.replace("\"", " ");
        text = text.replace("¿", " ");
        text = text.replace("?", " ");
        text = text.replace("¡", " ");
        text = text.replace("!", " ");
        text = text.replace("-", " ");
        text = text.replace("\\", " ");
        text = text.replace("_", " ");
        text = text.replace(",", " ");
        text = text.replace("\"", " ");
        text = text.replace("'", " ");
        return text;
    }

    /**
     * Reduce all words in the string to each stem.
     *
     * @param text Text to reduce.
     * @return Text with stems.
     */
    private String applyStemming(String text) {
        String textResult = new String();
        String delim = " ";
        String delimWord = "(?=\\p{Upper})";
        String[] textArray = text.split(delim);
        for (String word : textArray) {
            String[] wordArray;
            if (!StringUtils.isAllUpperCase(word) && !word.matches(".*\\d.*")) {
                // For i.e. AndroidRuntime or androidRuntime
                wordArray = word.split(delimWord);
            } else {
                wordArray = word.toLowerCase().split(delim);
            }
            if (wordArray.length > 1) {
                for (String subWord : wordArray) {
                    if (!stopWords.contains(subWord)/*&& !wordEnds(subWord)*/) {
                        textResult = textResult + " " + stemming(subWord);
                    } else {
                        textResult = textResult + " " + subWord;
                    }
                }
            } else {
                if (!stopWords.contains(word)/* && !wordEnds(word)*/) {
                    textResult = textResult + " " + stemming(word);
                } else {
                    textResult = textResult + " " + word;
                }
            }
        }
        return textResult;
    }

    /**
     * Applies the Lucene Snowball Porter Stemming.
     *
     * @param word To reduce.
     * @return The stem.
     */
    private String stemming(String word) {
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    /**
     * Loads the stopword in memory to help in the performance of applyStemming
     * function.
     */
    private void loadStopWords() {
        this.stopWords = new String();
        PropertiesApp.getInstance().fileLoad(ParametersEnum.LUCENE_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        File f = new File(PropertiesApp.getInstance().getPropertie(ParametersEnum.STOP_WORDS.getValue()));
        try {
            Scanner scanner = new Scanner(f);
            // Now read the file line by line...           
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stopWords = stopWords + " " + line;
            }
        } catch (FileNotFoundException e) {
            //handle this
        }
    }
}
