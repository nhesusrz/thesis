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
import dto.CommentDTO;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.ResourceBundle;
import logger.ThesisLogger;

public class CommentDAOImpl extends DAOManager implements CommentDAO {

    private int commentCount;

    public CommentDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM COMMENT");
                if (rs != null) {
                    rs.next();
                    commentCount = ((Long) rs.getObject(1)).intValue();
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.executeQuery: " + ex.toString());
            }
        } else {
            commentCount = 0;
        }
    }

    @Override
    public int getCount() {
        return commentCount;
    }

    @Override
    public int getCount(int bugID) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM COMMENT WHERE BUG_ID='" + bugID + "'");
            try {
                if (rs != null && rs.next()) {
                    int count = ((Long) rs.getObject(1)).intValue();
                    rs.close();
                    return count;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.getCommentCount: " + ex.toString());
            }
        }
        return 0;
    }

    @Override
    public int getCountBeetwDates(Date dateFrom, Date dateTo) {
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(COUNTER_ID) FROM COMMENT WHERE WHEN BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
                if (rs != null && rs.next()) {
                    int count = ((Long) rs.getObject(1)).intValue();
                    rs.close();
                    return count;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.getCountBeetwDates: " + ex.toString());
            }
        }
        return 0;
    }

    @Override
    public boolean insert(BaseDTO dto) {
        boolean error = false;
        if (connectionActive) {
            Date sqlDate = new java.sql.Date(((CommentDTO) dto).getDate().getTime());
            error = executeStatement("INSERT INTO COMMENT VALUES (null, '"
                    + ((CommentDTO) dto).getBugId() + "', '"
                    + ((CommentDTO) dto).getAuthor() + "', '"
                    + sqlDate.toString() + "', '"
                    + replaceSpecialCharactersSQL(((CommentDTO) dto).getText()) + "' );");
            if (!error) {
                commentCount++;
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("H2.Action.Run.PutComment"));
            }
        }
        return error;
    }

    @Override
    public BaseDTO get(int id) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM COMMENT WHERE COUNTER_ID='" + id + "'");
            try {
                while (rs != null && rs.next()) {
                    CommentDTO co = new CommentDTO();
                    co.setBugId((Integer) rs.getObject(2));
                    co.setAuthor((String) rs.getObject(3));
                    co.setDate(new java.util.Date(((Timestamp) rs.getObject(4)).getTime()));
                    co.setText((String) rs.getObject(5));
                    return co;
                }
                rs.close();
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.getComments: " + ex.toString());
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
        if (connectionActive) {
            List<BaseDTO> commentsList = new ArrayList<BaseDTO>();
            ResultSet rs = executeQuery("SELECT * FROM COMMENT WHERE WHEN BETWEEN '" + dateFrom.toString() + "' AND '" + dateTo.toString() + "'");
            try {
                while (rs != null && rs.next()) {
                    CommentDTO co = new CommentDTO();
                    co.setBugId((Integer) rs.getObject(2));
                    co.setAuthor((String) rs.getObject(3));
                    co.setDate(new java.util.Date(((Timestamp) rs.getObject(4)).getTime()));
                    co.setText((String) rs.getObject(5));
                    commentsList.add(co);
                }
                if (!commentsList.isEmpty()) {
                    return commentsList;
                }
                rs.close();
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.getComments: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public boolean delete() {
        commentCount = 0;
        if (connectionActive) {
            executeStatement("DELETE FROM COMMENT;");
            executeStatement("ALTER TABLE COMMENT ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

    @Override
    public List<CommentDTO> getComments(int bugID) {
        if (connectionActive) {
            List<CommentDTO> commentsList = new ArrayList<CommentDTO>();
            ResultSet rs = executeQuery("SELECT * FROM COMMENT WHERE BUG_ID='" + bugID + "'");
            try {
                while (rs != null && rs.next()) {
                    CommentDTO co = new CommentDTO();
                    co.setAuthor((String) rs.getObject(3));
                    co.setDate(new java.util.Date(((Timestamp) rs.getObject(4)).getTime()));
                    co.setText((String) rs.getObject(5));
                    commentsList.add(co);
                }
                if (!commentsList.isEmpty()) {
                    return commentsList;
                }
                rs.close();
            } catch (SQLException ex) {
                ThesisLogger.get().error("CommentDAOImpl.getComments: " + ex.toString());
            }
        }
        return null;
    }

}
