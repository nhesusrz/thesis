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

import dto.BaseDTO;
import dto.BugDTO;
import dto.CommentDTO;
import dto.DocumentDTO;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.ResourceBundle;
import logger.ThesisLogger;
import util.Duration;

public class DocumentDAOImpl extends DAOManager implements DocumentDAO {

    private int ldaDocCount;

    public DocumentDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM DOCUMENT");
                rs.next();
                ldaDocCount = ((Long) rs.getObject(1)).intValue();
            } catch (SQLException ex) {
                ThesisLogger.get().error("DocumentDAOImpl.executeQuery: " + ex.toString());
            }
        } else {
            ldaDocCount = 0;
        }
    }

    @Override
    public int getCount() {
        return ldaDocCount;
    }

    @Override
    public int getCountBeetwDates(Date dateFrom, Date dateTo) {
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(COUNTER_ID) FROM DOCUMENT WHERE DOC_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
                if (rs != null && rs.next()) {
                    int count = ((Long) rs.getObject(1)).intValue();
                    rs.close();
                    return count;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("DocumentDAOImpl.getCountBeetwDates: " + ex.toString());
            }
        }
        return 0;
    }

    @Override
    public boolean insert(BaseDTO dto) {
        if (connectionActive) {
            sqlDate = new java.sql.Date(((DocumentDTO) dto).getDate().getTime());
            boolean isCountable = executeStatement("INSERT INTO DOCUMENT VALUES (null, '"
                    + sqlDate.toString() + "', '"
                    + replaceSpecialCharactersSQL(((DocumentDTO) dto).getText()) + "' );");
            if (!isCountable) {
                ldaDocCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public BaseDTO get(int id) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM DOCUMENT WHERE COUNTER_ID='" + id + "'");
            try {
                if (rs != null && rs.next()) {
                    DocumentDTO doc = new DocumentDTO();
                    doc.setId(id);
                    java.util.Date javaDate = new java.util.Date(((Timestamp) rs.getObject(2)).getTime());
                    doc.setDate(javaDate);
                    doc.setText((String) rs.getObject(3));
                    rs.close();
                    javaDate = null;
                    return doc;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("DocumentDAOImpl.get: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public boolean exist(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<BaseDTO> getBeetwDates(Date dateFrom, Date dateTo) {
        List<BaseDTO> docsResult = new ArrayList<BaseDTO>();
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT COUNTER_ID, DOC_DATE, DOC_TEXT FROM DOCUMENT WHERE DOC_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
            if (rs != null) {
                try {
                    while (rs.next()) {
                        DocumentDTO doc = new DocumentDTO();
                        doc.setId((Integer) rs.getObject(1));
                        java.util.Date javaDate = new java.util.Date(((Timestamp) rs.getObject(2)).getTime());
                        doc.setDate(javaDate);
                        doc.setText((String) rs.getObject(3));
                        docsResult.add(doc);
                    }
                    rs.close();
                } catch (SQLException ex) {
                    ThesisLogger.get().error("DocumentDAOImpl.getBeetwDates: " + ex.toString());
                }
            }
        }
        return docsResult;
    }

    @Override
    public boolean delete() {
        if (connectionActive) {
            ldaDocCount = 0;
            executeStatement("DELETE FROM DOCUMENT;");
            executeStatement("ALTER TABLE DOCUMENT ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

    @Override
    public List<BaseDTO> getDocsFromVersion(Integer versionId) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM VERSION WHERE COUNTER_ID='" + versionId + "'");
            try {
                if (rs != null && rs.next()) {
                    java.sql.Date javaDateFrom = new java.sql.Date(((Timestamp) rs.getObject(2)).getTime());
                    java.sql.Date javaDateTo = new java.sql.Date(((Timestamp) rs.getObject(3)).getTime());
                    return this.getBeetwDates(javaDateFrom, javaDateTo);
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("DocumentDAOImpl.getDocsFromVersion: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public Integer getDocsCountFromVersion(Integer versionId) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM VERSION WHERE COUNTER_ID='" + versionId + "'");
            try {
                if (rs != null && rs.next()) {
                    java.sql.Date javaDateFrom = new java.sql.Date(((Timestamp) rs.getObject(2)).getTime());
                    java.sql.Date javaDateTo = new java.sql.Date(((Timestamp) rs.getObject(3)).getTime());
                    return this.getCountBeetwDates(javaDateFrom, javaDateTo);
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("DocumentDAOImpl.getDocsCountFromVersion: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public boolean generateDocsForLDA() {
        boolean error = false;
        int docLDAcountTemp = 0;
        delete();
        if (connectionActive) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.Inic"));
            long start = System.currentTimeMillis();
            if (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).getCount() > 0) {
                for (int i = 1; i <= DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).getCount(); i++) {
                    BugDTO bug = (BugDTO) DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).get(i);
                    sqlDate = new java.sql.Date(bug.getOpenedDate().getTime());
                    boolean isCountable = executeStatement("INSERT INTO DOCUMENT VALUES (null, '"
                            + sqlDate.toString() + "', '"
                            + replaceSpecialCharactersSQL(bug.getTitle() + " " + bug.getDescription()) + "' );");
                    if (!isCountable) {
                        docLDAcountTemp++;
                        setChanged();
                        notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.Run"));
                    } else {
                        error = true;
                    }
                    ArrayList<CommentDTO> comments = (ArrayList<CommentDTO>) ((CommentDAO) DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName())).getComments(i);
                    if (comments != null) {
                        for (CommentDTO co : comments) {
                            if (!co.getText().equals("") && !bug.getDescription().equals("")) {
                                sqlDate = new java.sql.Date(co.getDate().getTime());
                                isCountable = executeStatement("INSERT INTO DOCUMENT VALUES (null, '"
                                        + sqlDate.toString() + "', '"
                                        + replaceSpecialCharactersSQL(co.getText()) + "' );");
                                if (!isCountable) {
                                    docLDAcountTemp++;
                                    setChanged();
                                    notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.Run"));
                                } else {
                                    error = true;
                                }
                            }
                        }
                    }
                }
                long end = System.currentTimeMillis() - start;
                ldaDocCount = +docLDAcountTemp;
                setChanged();
                notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Mensage1"), docLDAcountTemp, Duration.getDurationBreakdown(end)));                                      
            } else {
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Error1"));
            }
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.End"));
        }
        return error;
    }

}
