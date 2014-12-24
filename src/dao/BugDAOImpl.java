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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.ResourceBundle;
import logger.ThesisLogger;

public class BugDAOImpl extends DAOManager implements BugDAO {

    private int bugCount;

    public BugDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM BUG");
                if (rs != null) {
                    rs.next();
                    bugCount = ((Long) rs.getObject(1)).intValue();
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("BugDAOImpl.executeQuery: " + ex.toString());
            }
        } else {
            bugCount = 0;
        }
    }

    @Override
    public int getCount() {
        return bugCount;
    }

    @Override
    public int getCountBeetwDates(Date dateFrom, Date dateTo) {
        if (connectionActive) {
            try {
                ResultSet rs;
                rs = executeQuery("SELECT COUNT(COUNTER_ID) FROM BUG WHERE OPENED_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
                if (rs != null && rs.next()) {
                    int count = ((Long) rs.getObject(1)).intValue();
                    rs.close();
                    return count;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("BugDAOImpl.getCountBeetwDates: " + ex.toString());
            }
        }
        return 0;
    }

    @Override
    public int getCountBeetwDatesClosed(Date dateFrom, Date dateTo, boolean closed) {
        if (connectionActive) {
            try {
                ResultSet rs;
                if (closed) {
                    rs = executeQuery("SELECT COUNT(COUNTER_ID) FROM BUG WHERE CLOSED_DATE IS NOT NULL AND OPENED_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
                } else {
                    rs = executeQuery("SELECT COUNT(COUNTER_ID) FROM BUG WHERE CLOSED_DATE IS NULL AND OPENED_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
                }
                if (rs != null && rs.next()) {
                    int count = ((Long) rs.getObject(1)).intValue();
                    rs.close();
                    return count;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("BugDAOImpl.getCountBeetwDatesClosed: " + ex.toString());
            }
        }
        return 0;
    }

    @Override
    public boolean insert(BaseDTO dto) {
        boolean error = false;
        if (connectionActive) {            
            String sqlClosedDate;
            if (((BugDTO) dto).getClosedDate() != null) {
                sqlClosedDate = "'" + (new java.sql.Date(((BugDTO) dto).getClosedDate().getTime())).toString() + "'";
            } else {
                sqlClosedDate = "null";
            }
            this.sqlDate = new java.sql.Date(((BugDTO) dto).getOpenedDate().getTime());
            error = executeStatement("INSERT INTO BUG VALUES (null, '"
                    + ((BugDTO) dto).getID() + "', '"
                    + replaceSpecialCharactersSQL(((BugDTO) dto).getTitle()) + "', '"
                    + ((BugDTO) dto).getStatus() + "', '"
                    + ((BugDTO) dto).getOwner() + "', '"
                    + ((BugDTO) dto).getType() + "', '"
                    + ((BugDTO) dto).getPriority() + "', '"
                    + ((BugDTO) dto).getComponent() + "', " 
                    + sqlClosedDate + ", '"                                  
                    + ((BugDTO) dto).getStars() + "', '"
                    + ((BugDTO) dto).getReportedBY() + "', '"
                    + this.sqlDate.toString() + "', '"
                    + replaceSpecialCharactersSQL(((BugDTO) dto).getDescription()) + "' );");
            if (!error) {
                bugCount++;
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Action.Run.PutBug"));
            }
            for (Iterator<CommentDTO> it = ((BugDTO) dto).getComments().iterator(); it.hasNext();) {
                CommentDTO comment = it.next();
                comment.setBugId(bugCount);
                DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName()).insert(comment);
            }
            closeStatement();
        }
        return error;
    }

    @Override
    public BaseDTO get(int id) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM BUG WHERE COUNTER_ID='" + id + "'");
            try {
                if (rs != null && rs.next()) {
                    BugDTO bug = new BugDTO();
                    bug.setID((Integer) rs.getObject(2));
                    bug.setTitle((String) rs.getObject(3));
                    bug.setStatus((String) rs.getObject(4));
                    bug.setOwner((String) rs.getObject(5));
                    bug.setType((String) rs.getObject(6));
                    bug.setPriority((String) rs.getObject(7));
                    bug.setComponent((String) rs.getObject(8));
                    java.util.Date javaDate = null;
                    if (rs.getObject(9) != null) {
                        javaDate = new java.util.Date(((Timestamp) rs.getObject(9)).getTime());
                        bug.setClosedON(true);
                        bug.setClosedDate(javaDate);
                    } else {
                        bug.setClosedON(false);
                        bug.setClosedDate(null);
                    }
                    bug.setStars((Integer) rs.getObject(10));
                    bug.setReportedBY((String) rs.getObject(11));
                    bug.setOpenedDate(new java.util.Date(((Timestamp) rs.getObject(12)).getTime()));
                    bug.setDescription((String) rs.getObject(13));
                    rs.close();
                    return bug;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("BugDAOImpl.get: " + ex.toString());
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
        List<BaseDTO> bugsResult = new ArrayList<BaseDTO>();
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM BUG WHERE OPENED_DATE BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
            if (rs != null) {
                try {
                    while (rs.next()) {
                        BugDTO bug = new BugDTO();
                        bug.setID((Integer) rs.getObject(2));
                        bug.setTitle((String) rs.getObject(3));
                        bug.setStatus((String) rs.getObject(4));
                        bug.setOwner((String) rs.getObject(5));
                        bug.setType((String) rs.getObject(6));
                        bug.setPriority((String) rs.getObject(7));
                        bug.setComponent((String) rs.getObject(8));
                        java.util.Date javaDate = new java.util.Date(((Timestamp) rs.getObject(9)).getTime());
                        if (javaDate.getYear() < 0) {
                            bug.setClosedON(false);
                            bug.setClosedDate(null);
                        } else {
                            bug.setClosedON(true);
                            bug.setClosedDate(javaDate);
                        }
                        bug.setStars((Integer) rs.getObject(10));
                        bug.setReportedBY((String) rs.getObject(11));
                        bug.setOpenedDate(new java.util.Date(((Timestamp) rs.getObject(12)).getTime()));
                        bug.setDescription((String) rs.getObject(13));
                        javaDate = null;
                        bugsResult.add(bug);
                    }
                    rs.close();
                } catch (SQLException ex) {
                    ThesisLogger.get().error("BugDAOImpl.getBeetwDates: " + ex.toString());
                }
            }
        }
        return bugsResult;
    }

    @Override
    public boolean delete() {
        bugCount = 0;
        if (connectionActive) {
            executeStatement("DELETE FROM BUG;");
            executeStatement("ALTER TABLE BUG ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

}
