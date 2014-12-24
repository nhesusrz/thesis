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

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import util.ParametersEnum;

public class LuceneManager extends Observable implements Observer {

    private static LuceneManager instance;
    private Analyzer analyzer;
    private Indexer indexer;
    private Searcher searcher;
    private Thread t;

    public LuceneManager(ParametersEnum analyzerType) {
        instance = null;
        analyzer = getAnalyzer(analyzerType);
        indexer = null;
        searcher = null;
    }

    public LuceneManager() {
        instance = null;
        analyzer = null;
        indexer = null;
        searcher = null;
    }

    public void destroy() throws CorruptIndexException, IOException {
        indexer.destroy();
        searcher.destroy();
    }

    public static LuceneManager getInstance(ParametersEnum analyzerType) {
        if (instance == null) {
            instance = new LuceneManager(analyzerType);
        }
        return instance;
    }

    public static LuceneManager getInstance() {
        if (instance == null) {
            instance = new LuceneManager();
        }
        return instance;
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    private Analyzer getAnalyzer(ParametersEnum type) {
        if (type.equals("Whitespace Analyser")) {
            return new WhitespaceAnalyzer(Version.LUCENE_36);
        } else if (type.equals("Simple Analyzer")) {
            return new SimpleAnalyzer(Version.LUCENE_36);
        } else if (type.equals("Stop Analyzer")) {
            return new StopAnalyzer(Version.LUCENE_36);
        } else {
            return new StandardAnalyzer(Version.LUCENE_36);
        }
    }

    /**
     * Execute an indexer thread.
     *
     * @param indexDir Directory of the index.
     * @param incremental True if add more documents. False, if not.
     */
    public void indexing(File indexDir, boolean incremental) {
        try {
            indexer = new Indexer(analyzer, indexDir, incremental);
            indexer.addObserver(instance);

        } catch (CorruptIndexException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error3"));
        } catch (LockObtainFailedException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error4"));
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
        } catch (java.lang.NullPointerException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error5"));
        }
        t = new Thread(indexer);
        t.start();
    }

    /**
     * Stops the indexer thread.
     */
    public void stopIndexing() {
        t.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Mensage2"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.End"));
        try {
            indexer.destroy();
        } catch (CorruptIndexException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error3"));
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
        }
    }
    /**
     * Execute an searcher thread.
     * 
     * @param indexDir Directory of the index.
     * @param queryString Lucene query.
     * @param limit Result size.
     * @param clusteringAlgorithm
     */
    public void search(File indexDir, String queryString, int limit, ClusteringAlgorithm clusteringAlgorithm) {
        try {
            // if() Preguntar si el indice esta bien.
            searcher = new Searcher(analyzer, indexDir, queryString, limit, clusteringAlgorithm);
            searcher.addObserver(instance);
        } catch (CorruptIndexException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Error1"));
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error5"));
        } catch (ParseException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Error2"));
        } catch (java.lang.NullPointerException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Error5"));
        }
        t = new Thread(searcher);
        t.start();
    }

    public void stopSearching() {
        t.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Mensage2"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.End"));
        try {
            searcher.destroy();
        } catch (IOException e) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
        }
    }
}
