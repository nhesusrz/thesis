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
package dao;

import algorithms.GenDocs;
import dataBase.H2DB;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import logger.ThesisLogger;
import util.PropertiesApp;
import util.ParametersEnum;

public class DAOManager extends Observable implements Observer {

    private static DAOManager instance;
    private static HashMap<String, BaseDAO> daos;
    protected H2DB db;
    protected boolean connectionActive;

    protected Date sqlDate, sqlEmptyDate;

    public DAOManager() {
        PropertiesApp.getInstance().fileLoad(".//properties//h2.properties");
        try {
            db = new H2DB(PropertiesApp.getInstance().getPropertie(ParametersEnum.DB_URL.toString()),
                    PropertiesApp.getInstance().getPropertie(ParametersEnum.DB_USER.toString()),
                    PropertiesApp.getInstance().getPropertie(ParametersEnum.DB_PASS.toString()));
            db.addObserver(this);
            createConnection();
            runSquema(PropertiesApp.getInstance().getPropertie(ParametersEnum.DB_SQUEMA_URL.toString()));
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.openDB: " + ex.toString());
        }
        daos = new HashMap<String, BaseDAO>();
    }

    /**
     * Return the unique instance of the DAO Manager.
     *
     * @return Instance of this class.
     */
    public static DAOManager getInstance() {
        if (instance == null) {
            instance = new DAOManager();
        }
        return instance;
    }

    /**
     * Return an especific DAO.
     *
     * @param daoName The dao name.
     * @return BaseDAO.
     */
    public static BaseDAO getDAO(String daoName) {
        try {
            switch (daoName) {
                case "BUG_DAO":
                    if (!daos.containsKey(DAONameEnum.BUG_DAO.getName())) {
                        BugDAO bugDAO = new BugDAOImpl(instance);
                        daos.put(DAONameEnum.BUG_DAO.getName(), bugDAO);
                        return bugDAO;
                    } else {
                        return daos.get(DAONameEnum.BUG_DAO.getName());
                    }
                case "COMMENT_DAO":
                    if (!daos.containsKey(DAONameEnum.COMMENT_DAO.getName())) {
                        CommentDAO commentDAO = new CommentDAOImpl(instance);
                        daos.put(DAONameEnum.COMMENT_DAO.getName(), commentDAO);
                        return commentDAO;
                    } else {
                        return daos.get(DAONameEnum.COMMENT_DAO.getName());
                    }
                case "TERM_DAO":
                    if (!daos.containsKey(DAONameEnum.TERM_DAO.getName())) {
                        TermDAO termDAO = new TermDAOImpl(instance);
                        daos.put(DAONameEnum.TERM_DAO.getName(), termDAO);
                        return termDAO;
                    } else {
                        return daos.get(DAONameEnum.TERM_DAO.getName());
                    }
                case "DOCUMENT_DAO":
                    if (!daos.containsKey(DAONameEnum.DOCUMENT_DAO.getName())) {
                        DocumentDAO documentDAO = new DocumentDAOImpl(instance);
                        daos.put(DAONameEnum.DOCUMENT_DAO.getName(), documentDAO);
                        return documentDAO;
                    } else {
                        return daos.get(DAONameEnum.DOCUMENT_DAO.getName());
                    }
                case "TOPIC_DAO":
                    if (!daos.containsKey(DAONameEnum.TOPIC_DAO.getName())) {
                        TopicDAO topicDAO = new TopicDAOImpl(instance);
                        daos.put(DAONameEnum.TOPIC_DAO.getName(), topicDAO);
                        return topicDAO;
                    } else {
                        return daos.get(DAONameEnum.TOPIC_DAO.getName());
                    }
                case "VERSION_DAO":
                    if (!daos.containsKey(DAONameEnum.VERSION_DAO.getName())) {
                        VersionDAO versionDAO = new VersionDAOImpl(instance);
                        daos.put(DAONameEnum.VERSION_DAO.getName(), versionDAO);
                        return versionDAO;
                    } else {
                        return daos.get(DAONameEnum.VERSION_DAO.getName());
                    }
                default:
                    throw new SQLException("DaoManager.getDAO: Can't create " + daoName);
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.getDAO: Can't create " + daoName + ex.toString());
        }
        return null;
    }

    /**
     * Deletes previous bugs and comments.
     *
     * @return True if were erased correctly.
     */
    public boolean deleteTablesForParsing() {
        getDAO(DAONameEnum.BUG_DAO.getName()).delete();
        getDAO(DAONameEnum.COMMENT_DAO.getName()).delete();
        //resetAllDaos();
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Mensage2"));
        return true;
    }

    /**
     * Execute a query (select statement) and returns the result set. If another
     * result set exists for this statement, this will be closed (even if this
     * statement fails).
     *
     * @param statement Is the statement to execute-
     * @return The results set.
     */
    protected ResultSet executeQuery(String statement) {
        try {
            return (db.executeQuery(statement));
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.executeQuery: " + ex.toString());
        }
        return null;
    }

    /**
     * Executes an arbitrary statement.
     *
     * @param statement The sql statement to execute.
     * @return returns true if a result set is available, false if not.
     */
    protected boolean executeStatement(String statement) {
        try {
            return (db.executeStatement(statement));
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.executeStatement: " + ex.toString());
        }
        return false;
    }

    /**
     * Closes this statement. All result sets that where created by this
     * statement become invalid after calling this method.
     *
     * @return Returns true if the statement successfully closed. False if not.
     */
    protected boolean closeStatement() {
        try {
            return db.closeStatement();
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.closeStatement: " + ex.toString());
        }
        return false;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        setChanged();
        notifyObservers(arg1);
    }

    /**
     * Creates a conecctio to data base.
     *
     * @throws SQLException
     */
    private void createConnection() throws SQLException {
        db.createConnection();
        connectionActive = true;
        setChanged();
        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Mensage1"));
    }

    /**
     * Closes the conecction with data base.
     */
    private void closeConecction() {
        try {
            db.closeConnection();
            connectionActive = false;
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Mensage4"));
        } catch (SQLException ex) {
            ThesisLogger.get().error("DaoManager.closeConecction: " + ex.toString());
        }
    }

    /**
     * Executes the data base squema.
     *
     * @param url The url to the squema file.
     */
    private void runSquema(String url) {
        InputStream is;
        try {
            is = new FileInputStream(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String line = br.readLine();
                StringBuilder statement = new StringBuilder();
                while (line != null) {
                    line = line.trim();
                    if (!line.startsWith("--") && !line.startsWith("#") && !line.startsWith("//")) {
                        statement.append(line);
                        if (line.endsWith(";")) {
                            executeStatement(statement.toString());
                            statement = new StringBuilder();
                        }
                    }
                    line = br.readLine();
                }
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Mensage3"));
            } catch (IOException ex) {
                Logger.getLogger(GenDocs.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            ThesisLogger.get().error("DaoManager.closeConecction: " + ex.toString());
        }
    }

    /**
     * Executes the reset method of each dao.
     */
    private void resetAllDaos() {
        for (Map.Entry<String, BaseDAO> entry : daos.entrySet()) {
            BaseDAO baseDAO = entry.getValue();
            baseDAO.delete();
        }
    }

    /**
     * Replaces special sql characters .
     *
     * @param source The string source.
     * @return The string with special characters replaced.
     */
    protected String replaceSpecialCharactersSQL(String source) {
        return source.replaceAll("'", "''");
    }

}
