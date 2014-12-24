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
import org.xml.sax.SAXException;
import util.ParametersEnum;

public class Indexer extends Observable implements Runnable {

    private IndexWriter index_writer;
    private int docsCount;
    private SimpleFSDirectory directory;

    public Indexer(Analyzer analyzer, File indexDir, File dataDir, boolean incremental) throws CorruptIndexException, LockObtainFailedException, IOException {
        index_writer = new IndexWriter(new SimpleFSDirectory(indexDir), analyzer, incremental, MaxFieldLength.UNLIMITED);
        docsCount = 0;
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
        notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Mensage1"), docsCount, (end - start)));
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
            //Bug bug = DBController.getInstance().getBug(i);            
            dto.DocumentDTO docSource = (dto.DocumentDTO) ((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).get(i);
            Document docLucene = new Document();
            Field field = new Field(ParametersEnum.INDEX_FIELD1.toString(), Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.YES);
            docLucene.add(field);
            //System.out.println("METO DATE: " + docSource.getDate().toString());
            //System.out.println("O METO DATE: " + DateTools.timeToString(docSource.getDate().getTime(), DateTools.Resolution.MINUTE).toString());
            field = new Field(ParametersEnum.INDEX_FIELD2.toString(), DateTools.timeToString(docSource.getDate().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.YES);
            //field.setBoost((float)0.2);
            docLucene.add(field);
            field = new Field(ParametersEnum.INDEX_FIELD3.toString(), docSource.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
            //field.setBoost((float) 1.5);
            docLucene.add(field);
            //Field.setOmitTermFreqAndPositions(true);           
            index_writer.addDocument(docLucene);
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.Run"));
        }
    }
}
