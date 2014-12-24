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
import dto.TopicDTO;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import logger.ThesisLogger;

public class TopicDAOImpl extends DAOManager implements TopicDAO {

    private int topicCount;

    public TopicDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM TOPIC");
                if (rs != null) {
                    rs.next();
                    topicCount = ((Long) rs.getObject(1)).intValue();
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("TopicDAOImpl.executeQuery: " + ex.toString());
            }
        } else {
            topicCount = 0;
        }
    }

    @Override
    public int getCount() {
        return topicCount;
    }

    @Override
    public int getCountBeetwDates(Date dateFrom, Date dateTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean insert(BaseDTO dto) {
        boolean error = false;
        if (connectionActive) {
            if (!((TopicDTO) dto).isEmptyDocs() && !((TopicDTO) dto).isEmptyWords()) {
                error = executeStatement("INSERT INTO TOPIC VALUES (null);");
                if (!error) {
                    topicCount++;
                }
                error = insertTopicTerm((TopicDTO) dto);
                error = insertTopicDocument((TopicDTO) dto);
            }
        }
        return error;
    }

    @Override
    public BaseDTO get(int id) {
        if (exist(id)) {
            TopicDTO topic = new TopicDTO(new Integer(id));
            topic.setDataWords(getTopicWords(id).getDataWords());
            topic.setDataDocs(getTopicDocs(id).getDataDocs());
            return topic;
        }
        return null;
    }

    @Override
    public boolean exist(int id) {
        ResultSet rs = executeQuery("SELECT COUNTER_ID from TOPIC where COUNTER_ID = '" + id + "';");
        try {
            if (rs != null && rs.next()) {
                int count = (((Integer) rs.getObject(1))).intValue();
                if (count > 0) { // Verifico que el term ya no exista.
                    return true;
                }
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("TopicDAOImpl.exist: " + ex.toString());
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
            topicCount = 0;
            executeStatement("DELETE FROM TOPIC;");
            executeStatement("ALTER TABLE TOPIC ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            executeStatement("DELETE FROM TOPIC_DOCUMENT;");
            executeStatement("ALTER TABLE TOPIC_DOCUMENT ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            executeStatement("DELETE FROM TOPIC_TERM;");
            executeStatement("ALTER TABLE TOPIC_TERM ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

    @Override
    public TopicDTO getTopicWords(int topicID) {
        if (connectionActive) {
            TopicDTO topic = new TopicDTO(new Integer(topicID));
            ResultSet rs = executeQuery("SELECT * FROM TOPIC_TERM WHERE TOPIC_ID='" + topicID + "'");
            try {
                while (rs != null && rs.next()) {
                    topic.putWord((String) rs.getObject(3), (BigDecimal) rs.getObject(4));
                }
                if (!topic.isEmptyWords()) {
                    rs.close();
                    return topic;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("TopicDAOImpl.getTopicWords: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public TopicDTO getTopicDocs(int topicID) {
        if (connectionActive) {
            TopicDTO topic = new TopicDTO(new Integer(topicID));
            ResultSet rs = executeQuery("SELECT DOC_ID, PROBABILITY  FROM TOPIC_DOCUMENT WHERE TOPIC_ID='" + topicID + "'");
            try {
                while (rs != null && rs.next()) {
                    topic.putDoc((Integer) rs.getObject(1), (BigDecimal) rs.getObject(2));
                }
                if (!topic.isEmptyDocs()) {
                    rs.close();
                    return topic;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("TopicDAOImpl.getTopicDocs: " + ex.toString());
            }
        }
        return null;
    }

    private boolean insertTopicTerm(TopicDTO topicDTO) {
        boolean error = false;
        if (connectionActive) {
            for (Iterator<Map.Entry<String, BigDecimal>> it = topicDTO.getDataWords().entrySet().iterator(); topicDTO.getDataWords() != null && it.hasNext();) {
                Map.Entry<String, BigDecimal> entry = it.next();
                DAOManager.getDAO(DAONameEnum.TERM_DAO.getName()).insert(new TermDTO(entry.getKey()));
                error = executeStatement("INSERT INTO TOPIC_TERM VALUES (null, '"
                        + topicCount + "', '"
                        + replaceSpecialCharactersSQL(entry.getKey()) + "', '"
                        + entry.getValue() + "');");
            }
            closeStatement();
        }
        return error;
    }

    private boolean insertTopicDocument(TopicDTO topicDTO) {
        boolean error = false;
        if (connectionActive) {
            for (Iterator<Map.Entry<Integer, BigDecimal>> it = topicDTO.getDataDocs().entrySet().iterator(); topicDTO.getDataWords() != null && it.hasNext();) {
                Map.Entry<Integer, BigDecimal> entry = it.next();
                error = executeStatement("INSERT INTO TOPIC_DOCUMENT VALUES (null, '"
                        + entry.getKey() + "', '"
                        + topicCount + "', '"
                        + entry.getValue() + "');");
            }
            closeStatement();
        }
        return error;
    }

}
