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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logger.ThesisLogger;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.IClusteringAlgorithm;
import org.carrot2.core.ProcessingComponentConfiguration;
import org.carrot2.source.lucene.LuceneDocumentSource;

public class ClusteringAlgorithm {

    private final IClusteringAlgorithm algorithm;
    private final String name;

    public ClusteringAlgorithm(IClusteringAlgorithm algorithm, String nombre) {        
        this.algorithm = algorithm;
        this.name = nombre;
    }

    @Override
    public String toString() {
        return name;
    }

    public IClusteringAlgorithm getAlgoritmo() {
        return algorithm;
    }
    /**
     * Performs processing result of the clustering algorithm.
     * 
     * @param indexPath Index url path.
     * @param scd_list Lucene documents.
     * @return List of Carrot clusters.
     * @throws IOException 
     */
    public List<Cluster> Process(String indexPath, List scd_list) throws IOException {
        Controller carrotController = ControllerFactory.createSimple(); 
        ThesisLogger.get().addAppender(org.carrot2.log4j.BufferingAppender.attach("carrot2"));
        //ThesisLogger.get().getAppender("carrot2")
        Map<String, Object> luceneGlobalAttributes = new HashMap<String, Object>();
        carrotController.init(new HashMap<String, Object>(), new ProcessingComponentConfiguration(LuceneDocumentSource.class, "lucene", luceneGlobalAttributes));        
        return carrotController.process(scd_list, null, algorithm.getClass()).getClusters();
    }
}
