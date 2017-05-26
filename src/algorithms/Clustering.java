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
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import logger.ThesisLogger;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.IClusteringAlgorithm;
import org.carrot2.core.ProcessingComponentConfiguration;
import org.carrot2.source.lucene.LuceneDocumentSource;

public class Clustering extends Observable implements Runnable {

    private static Clustering instance = null;

    private final IClusteringAlgorithm algorithm;
    private final String name;    
    private List scd_list;
    private List<Cluster> result_list;
    

    public Clustering(IClusteringAlgorithm algorithm, String nombre) {
        this.algorithm = algorithm;
        this.name = nombre;        
        
    }

    public static Clustering getInstance(IClusteringAlgorithm algorithm, String nombre, List scd_list) {
        if ((instance == null) || ((instance != null) && !(instance.name.equals(nombre)))) {
            instance = new Clustering(algorithm, nombre);
        }
        instance.scd_list = scd_list;  // Replace the lucene's query documents.    
        return instance;
    }
    
    public static Clustering getInstance() {
        if (instance != null)
            return instance;
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public IClusteringAlgorithm getAlgoritmo() {
        return algorithm;
    }

    @Override
    public void run() {
        try {
            Process();
        } catch (IOException ex) {
            Logger.getLogger(Clustering.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return The list with clusters.
     */
    public List<Cluster> getResult() {
        return result_list;        
    }

    /**
     * Performs processing result of the clustering algorithm.
     *
     * @return List of Carrot clusters.
     * @throws IOException
     */
    private void Process() throws IOException {
        Controller carrotController = ControllerFactory.createSimple();
        ThesisLogger.get().addAppender(org.carrot2.log4j.BufferingAppender.attach("carrot2"));
        //ThesisLogger.get().getAppender("carrot2")
        Map<String, Object> luceneGlobalAttributes = new HashMap<String, Object>();
        carrotController.init(new HashMap<String, Object>(), new ProcessingComponentConfiguration(LuceneDocumentSource.class, "lucene", luceneGlobalAttributes));
        result_list = carrotController.process(scd_list, null, algorithm.getClass()).getClusters();
        setChanged();
        notifyObservers();              
    }
}
