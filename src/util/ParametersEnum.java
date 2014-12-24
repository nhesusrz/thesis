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
package util;

public enum ParametersEnum {

    XML_DEFAULT_PATH(".//data"),
    DB_DEFAULT_PATH(".//inputLDA"),
    INDEX_DEFAULT_PATH(".//index"),
    MALLET_PROPERTIE_FILE_DEFAULT_PATH(".//properties//malletLDA.properties"),
    DB_URL("db.url"),
    DB_NAME("db.name"),
    DB_USER("db.user"),
    DB_PASS("db.password"),
    DB_SQUEMA_URL("db.squema"),
    LDA_STOP_WORDS("lda.stopwords"),
    LDA_ITERATIONS("lda.iterations"),
    LDA_NUM_TOPICS("lda.num_topics"),
    LDA_ALPHA("lda.alpha"),
    LDA_BETA("lda.beta"),
    LDA_WORDS_PER_TOPIC("lda.wordsPerTopic"),
    LDA_STEP_DAY("Day"),
    LDA_STEP_MONTH("Month"),
    LDA_STEP_YEAR("Year"),
    INDEX_FIELD1("Id"),
    INDEX_FIELD2("Date"),
    INDEX_FIELD3("Text"),
    INDEX_DEFAULT(" Simple Analyzer"),
    INDEX_WHITESPACE_ANALYZER(" Whitespace Analyser"),
    INDEX_SIMPLE_ANALYZER(" Simple Analyzer"),
    INDEX_STOP_ANALYZER(" Stop Analyzer"),
    INDEX_STANDAR_ANALYZER(" Standar Analyzer"),
    INDEX_HITS(" HITS"),
    SEARCH_ALGORITHM_KMEANS("K - Means"),
    SEARCH_ALGORITHM_SUFFIX("Suffix Tree Clustering"),
    SEARCH_ALGORITHM_LINGO("Lingo"),
    SEARCH_ALGORITHM_SYNTHETIC("Synthetic");

    private final String value;

    ParametersEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
