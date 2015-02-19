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
package dto;

import dao.DAOManager;
import dao.DAONameEnum;
import dao.TopicDAO;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TopicDTO extends BaseDTO {

    private Integer id;
    private Date openedDate;
    private HashMap<String, BigDecimal> words;
    private HashMap<Integer, BigDecimal> docs;
    private BigDecimal sumWordsProb;
    private BigDecimal sumDocsProb;
    private final double varianceThreshold = 2.5;

    public TopicDTO() {
        id = -1;
        words = new HashMap<String, BigDecimal>();
        docs = new HashMap<Integer, BigDecimal>();
        sumWordsProb = sumDocsProb = new BigDecimal(0);
    }

    public TopicDTO(Integer id) {
        this.id = id;
        words = new HashMap<String, BigDecimal>();
        docs = new HashMap<Integer, BigDecimal>();
        sumWordsProb = sumDocsProb = new BigDecimal(0);
    }

    /**
     * Overrides this method and included HTML tagas.
     *
     * @return String.
     */
    @Override
    public String toString() {
        String result;
        if (getVarianceDocs().compareTo(new BigDecimal(varianceThreshold)) >= 0) {
            result = "<html><b><span style=\"color:#E60000;\">Z";
        } else {
            result = "<html><b><span style=\"color:#000000;\">Z";
        }
        result = result + id + "</b></span>:{ <i>";
        int i = 0;
        for (Iterator<Map.Entry<String, BigDecimal>> it = getSortedDataWords().entrySet().iterator(); it.hasNext() && (i < 5);) {
            Map.Entry<String, BigDecimal> entry = it.next();
            if (i == 4) {
                result += entry.getKey() + "</i> }</html>";
            } else {
                result += entry.getKey() + ", ";
            }
            i++;
        }
        if (i <= 2) {
            result = result.replace(", ", " ");
            result += "</i> }</html>";
        }
        return result;
    }

    /**
     * Returns an string without HTML tags.
     *
     * @return String.
     */
    public String toStringWithoutHTMLTags() {
        String result = "Z" + id + "{";
        int i = 0;
        for (Iterator<Map.Entry<String, BigDecimal>> it = getSortedDataWords().entrySet().iterator(); it.hasNext() && (i < 3);) {
            Map.Entry<String, BigDecimal> entry = it.next();
            if (i == 2) {
                result += entry.getKey() + "}";
            } else {
                result += entry.getKey() + ", ";
            }
            i++;
        }
        return result;
    }

    public Integer getID() {
        return id;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public HashMap<String, BigDecimal> getDataWords() {
        return words;
    }

    public void setDataWords(HashMap<String, BigDecimal> words) {
        this.words = words;
    }

    public HashMap<String, BigDecimal> getSortedDataWords() {
        return sortByComparator(words);
    }

    public void putWord(String word, BigDecimal probability) {
        words.put(word, probability);
        sumWordsProb = sumWordsProb.add(probability);
    }

    public BigDecimal getTermProb(String termID) {
        if (words != null && words.get(termID) != null) {
            return words.get(termID);
        }
        return null;
    }

    public boolean isEmptyWords() {
        return (words.isEmpty());
    }

    public HashMap<Integer, BigDecimal> getDataDocs() {
        return docs;
    }

    public void setDataDocs(HashMap<Integer, BigDecimal> docs) {
        this.docs = docs;
    }

    public HashMap<Integer, BigDecimal> getSortedDataDocs() {
        return sortByComparator2(docs);
    }

    public void putDoc(Integer docID, BigDecimal probability) {
        docs.put(docID, probability);
        sumDocsProb = sumDocsProb.add(probability);
    }

    public BigDecimal getDocProb(Integer docID) {
        if (docs != null && docs.get(docID) != null) {
            return docs.get(docID);
        }
        return new BigDecimal(0);
    }

    public int getDocsCount() {
        return docs.size();
    }

    public boolean isEmptyDocs() {
        return (docs.isEmpty());
    }

    /**
     * Order the topic's term by his probability.
     *
     * @param unsortMap No ordered terms.
     * @return An ordered term hashmap.
     */
    private static HashMap<String, BigDecimal> sortByComparator(HashMap<String, BigDecimal> unsortMap) {
        // Convert Map to List
        List<Map.Entry<String, BigDecimal>> list = new LinkedList<Map.Entry<String, BigDecimal>>(unsortMap.entrySet());
        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
            public int compare(Map.Entry<String, BigDecimal> o1, Map.Entry<String, BigDecimal> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        // Convert sorted map back to a Map
        HashMap<String, BigDecimal> sortedMap = new LinkedHashMap<String, BigDecimal>();
        for (Iterator<Map.Entry<String, BigDecimal>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, BigDecimal> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Order the topic's docs by his probability.
     *
     * @param unsortMap No ordered terms.
     * @return An ordered term hashmap.
     */
    private static HashMap<Integer, BigDecimal> sortByComparator2(HashMap<Integer, BigDecimal> unsortMap) {
        // Convert Map to List
        List<Map.Entry<Integer, BigDecimal>> list = new LinkedList<Map.Entry<Integer, BigDecimal>>(unsortMap.entrySet());
        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Integer, BigDecimal>>() {
            public int compare(Map.Entry<Integer, BigDecimal> o1, Map.Entry<Integer, BigDecimal> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        // Convert sorted map back to a Map
        HashMap<Integer, BigDecimal> sortedMap = new LinkedHashMap<Integer, BigDecimal>();
        for (Iterator<Map.Entry<Integer, BigDecimal>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, BigDecimal> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Calculates the the statistical variance in the document's probabilities.
     *
     * @return The variance.
     */
    private BigDecimal getVarianceDocs() {
        BigDecimal result = new BigDecimal(0);
        if (sumDocsProb.compareTo(result) <= 0) {
            for (Map.Entry<Integer, BigDecimal> entry : docs.entrySet()) {
                sumDocsProb = sumDocsProb.add(entry.getValue());
            }
        }
        BigDecimal mu = sumDocsProb.divideToIntegralValue(new BigDecimal(docs.size()));
        for (Map.Entry<Integer, BigDecimal> entry : docs.entrySet()) {
            result = result.add(entry.getValue().subtract(mu).abs().pow(2));
        }
        if (((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount() > 0) {
            return result.divideToIntegralValue(new BigDecimal(((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount()));
        }
        return new BigDecimal(0);
    }

}
