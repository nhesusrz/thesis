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
import dao.VersionDAO;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import util.ParametersEnum;

public class AlgorithmManager extends Observable implements Observer {

    public static AlgorithmManager instance;
    private static Thread thread;

    public AlgorithmManager() {
    }

    public void destroy() {
        thread = null;
    }

    public static AlgorithmManager getInstance() {
        if (instance == null) {
            instance = new AlgorithmManager();
        }
        return instance;
    }

    /**
     * Execute the parsing process.
     *
     * @param fileName Data set url
     * @param resetDB Indicates if comments and comments will be deleted
     */
    public void parse(String fileName, boolean resetDB) {
        if (fileName != null) {
            thread = new Thread(new Parser(fileName, resetDB, this));
            thread.start();
        } else {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Parser.Error3"));
        }
    }

    /**
     * Stop the parsing process.
     */
    public void stopParse() {
        thread.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Parser.Mensage3"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"));
    }

    /**
     * Execute the generating version process.
     *
     * @param type This is the type (Day, Month and Year) version.
     * @param step Indicates the number of type (Day, Month and Year).
     * @param dateFrom Is the first date of the period.
     * @param dateTo Is the last date of the period.
     */
    public void generateVersions(ParametersEnum type, Integer step, java.sql.Date dateFrom, java.sql.Date dateTo) {
        GenVersions.getInstance().addObserver(this);
        ((VersionDAO) DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).setParametersForGen(type, step, dateFrom, dateTo);
        thread = new Thread(GenVersions.getInstance());
        thread.start();
    }

    /**
     * Stop the generating version process.
     */
    public void stopGenerateVersions() {
        thread.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Mensage2"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.End"));
    }

    /**
     * Execute the generating documments process. Needs bugs and comments in the
     * data base.
     */
    public void generateDocs() {
        GenDocs.getInstance().addObserver(this);
        thread = new Thread(GenDocs.getInstance());
        thread.start();
    }

    /**
     * Stop the generating documents process.
     */
    public void stopGenerateDocs() {
        thread.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Mensage2"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.End"));
    }

    /**
     * Execute the Mallet LDA algorithm for extract topics from de previous
     * generated documents.
     *
     * @param numTopics Indicates the number of topics will be generated.
     * @param alpha Dirichlet(alpha,alpha,...) is the distribution over topics.
     * @param beta Prior on per-topic multinomial distribution over words.
     * @param wordsPerTopic Number of words per topic.
     * @param removeStopWords Indicates if stop words will be removed.
     * @param dateFrom Is the first date of the period.
     * @param dateTo Is the last date of the period.
     */
    public void lda(int numIerations, int numTopics, double alpha, double beta, int wordsPerTopic, boolean removeStopWords, Date dateFrom, Date dateTo) {
        SimpleLDAGibbs.getInstance(numIerations, numTopics, alpha, beta, wordsPerTopic, removeStopWords, dateFrom, dateTo).addObserver(this);
        thread = new Thread(SimpleLDAGibbs.getInstance());
        thread.start();
    }

    /**
     * Stop the Mallet LDA algorithm.
     */
    public void stopLDA() {
        thread.stop();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Mensage3"));
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.End"));
    }

    /**
     * This method exits because in the SAX parser class can't extends from
     * Observable.
     */

    public void notify(String msj) {
        setChanged();
        notifyObservers(msj);
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers((String) arg);
    }

}
