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

import algorithms.ClusteringAlgorithm;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;
import logger.ThesisLogger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.carrot2.core.Cluster;
import org.carrot2.core.Document;
import util.ParametersEnum;

public class Searcher extends Observable implements Runnable {

    private final Analyzer analyzer;
    private final IndexReader index_reader;
    private IndexSearcher indexSearcher;
    private final QueryParser mqp;
    private final Query query;
    private final int counter;   
    private final String indexDir;
    private List<Document> scd_list;
    private final ClusteringAlgorithm clusteringAlgorithm;

    public Searcher(Analyzer analyer, File indexDir, String frase, int counter, ClusteringAlgorithm clusteringAlgorithm) throws CorruptIndexException, IOException, ParseException {
        this.indexDir = indexDir.getPath();
        index_reader = IndexReader.open(new SimpleFSDirectory(indexDir));
        indexSearcher = new IndexSearcher(index_reader);
        this.analyzer = analyer;
        this.counter = counter;
        this.clusteringAlgorithm = clusteringAlgorithm;
        mqp = new MultiFieldQueryParser(Version.LUCENE_32, new String[]{ParametersEnum.INDEX_FIELD3.toString()}, analyzer);
        query = mqp.parse(frase);
    }

    @Override
    public void run() {
        try {
            search();
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
        } catch (java.text.ParseException ex) {
            ThesisLogger.get().error("Searcher.run: " + ex.toString());
        }
    }

    public void destroy() throws IOException {
        index_reader.close();
        indexSearcher = null;
        scd_list = null;
    }

    /**
     * Executes the search query.
     *
     * @throws IOException
     */
    private void search() throws IOException, java.text.ParseException {
        normalSearchWithAlgorithm();
    }
    /**
     * Executes the searching process.
     *
     * @throws IOException
     */
    private void normalSearchWithAlgorithm() throws IOException, java.text.ParseException {
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.Inic"));
        long start = new Date().getTime();
        ScoreDoc[] scoreDocs = indexSearcher.search(query, counter).scoreDocs;
        List<Cluster> clusters = new ArrayList();
        if(scoreDocs.length > 1) {
            clusters = clusteringAlgorithm.Process(indexDir, ConvertLuceneDocsToCarrotDocs(scoreDocs));
        }
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.End"));
        setChanged();
        notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Mensage3"), clusters.size(), scoreDocs.length, System.currentTimeMillis() - start));
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            List<Document> cluster_docs = cluster.getDocuments();
            if(!cluster_docs.isEmpty()) {
                List<String> frases = cluster.getPhrases();
                setChanged();
                notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Mensage4"), (i + 1), frases.toString()));
                for (Document doc : cluster_docs) {
                    setChanged();
                    notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Mensage5"), 
                            doc.getField(ParametersEnum.INDEX_FIELD1.toString()), 
                            DateTools.stringToDate((String) doc.getField(ParametersEnum.INDEX_FIELD2.toString())),                           
                            (Float) doc.getField(ParametersEnum.INDEX_FIELD4.toString())));
                }                    
            }
        }
    }
    /**
     * Maps the Lucene documents to Carrot documents.
     *
     * @param scd Array of Lucene documents.
     * @throws CorruptIndexException
     * @throws IOException
     */
    private List<Document> ConvertLuceneDocsToCarrotDocs(ScoreDoc[] scd) throws CorruptIndexException, IOException {
        scd_list = new ArrayList<Document>(); // Inicio la lista de Documentos Carrot.        
        for (ScoreDoc scd1 : scd) {
            Document d = new Document(
                /*indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD1.toString()).stringValue(), 
                indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD2.toString()).stringValue(), */
                indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD3.toString()).stringValue());
            d.setField(ParametersEnum.INDEX_FIELD1.toString(), indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD1.toString()).stringValue());
            d.setField(ParametersEnum.INDEX_FIELD2.toString(), indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD2.toString()).stringValue());
            //d.setField(ParametersEnum.INDEX_FIELD3.toString(), indexSearcher.doc(scd1.doc).getField(ParametersEnum.INDEX_FIELD3.toString()).stringValue());
            d.setField(ParametersEnum.INDEX_FIELD4.toString(), scd1.score);
            scd_list.add(d);
        }
        return scd_list;
    }
}
