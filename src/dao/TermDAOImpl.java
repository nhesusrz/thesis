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
import dto.TermDTO;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import logger.ThesisLogger;

public class TermDAOImpl extends DAOManager implements TermDAO {

    private int termCount;

    public TermDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM TERM");
                if (rs != null) {
                    rs.next();
                    termCount = ((Long) rs.getObject(1)).intValue();
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("TermDAOImpl.getCount: " + ex.toString());
            }
        } else {
            termCount = 0;
        }
    }

    @Override
    public int getCount() {
        return termCount;
    }

    @Override
    public int getCountBeetwDates(Date dateFrom, Date dateTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean insert(BaseDTO dto) {
        boolean error = false;
        if (connectionActive) {
            if (!existTerm((TermDTO) dto)) {
                error = executeStatement("INSERT INTO TERM VALUES (null, '"
                        + replaceSpecialCharactersSQL(((TermDTO) dto).getWord()) + "');");
                if (!error) {
                    termCount++;
                }
            }
        }
        closeStatement();
        return error;
    }

    @Override
    public BaseDTO get(int id) {
        TermDTO term;
        ResultSet rs = executeQuery("SELECT COUNTER_ID, WORD FROM TERM WHERE COUNTER_ID ='" + id + "';");
        try {
            if (rs != null) {
                term = new TermDTO((Integer) rs.getObject(1), (String) rs.getObject(2));
                rs.close();
                return term;
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TermDAOImpl.getAllTerm: " + ex.toString());
        }
        return null;
    }

    @Override
    public boolean exist(int id) {
        ResultSet rs = executeQuery("SELECT COUNTER_ID from TERM where COUNTER_ID = '" + id + "';");
        try {
            if (rs != null && rs.next()) {
                int count = (((Long) rs.getObject(1))).intValue();
                if (count > 0) { // Verifico que el term ya no exista.
                    return true;
                }
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TermDAOImpl.exist: " + ex.toString());
        }
        return false;
    }

    @Override
    public List<BaseDTO> getBeetwDates(Date dateFrom, Date dateTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete() {
        if (connectionActive) {
            termCount = 0;
            executeStatement("DELETE FROM TERM;");
            executeStatement("ALTER TABLE TERM ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

    @Override
    public int getTermId(String word) {
        ResultSet rs = executeQuery("SELECT COUNTER_ID FROM TERM WHERE WORD = '" + replaceSpecialCharactersSQL(word) + "';");
        try {
            if (rs != null && rs.next()) {
                int res = ((Integer) rs.getObject(1)).intValue();
                rs.close();
                return res;
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TermDAOImpl.getTermId: " + ex.toString());
        }
        return -1;
    }

    @Override
    public boolean existTerm(TermDTO dto) {
        ResultSet rs = executeQuery("SELECT COUNT (COUNTER_ID) from TERM where word = '" + replaceSpecialCharactersSQL(dto.getWord()) + "';");
        try {
            if (rs != null && rs.next()) {
                int count = (((Long) rs.getObject(1))).intValue();
                if (count > 0) { // Verifico que el term ya no exista.
                    return true;
                }
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TermDAOImpl.existTerm: " + ex.toString());
        }
        return false;
    }

    @Override
    public List<String> getAllTerm() {
        List<String> res = new ArrayList<String>();
        ResultSet rs = executeQuery("SELECT WORD FROM TERM;");
        try {
            if (rs != null) {
                while (rs.next()) {
                    res.add((String) rs.getObject(1));
                }
                rs.close();
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TermDAOImpl.getAllTerm: " + ex.toString());
        }
        return res;
    }

}
