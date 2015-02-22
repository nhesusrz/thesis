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
package charts.data;

import java.util.HashMap;
import java.util.Observable;

public class Generator extends Observable {

    private static Thread metricThread, bugCommentThread, bugComponentThread = null;

    public Generator() {
    }

    /**
     * Excutes the data generation for bug and comments chart in a thread.
     *
     * @param dataChartGenerator
     */
    public static void generateBugCommentData(BugCommentDistributionData dataChartGenerator) {
        if (bugCommentThread == null) {
            bugCommentThread = new Thread(dataChartGenerator);
        }
        bugCommentThread.start();
    }

    /**
     * Excutes the stop BugComment thread.
     */
    public static void stopBugCommentThread() {
        if (bugCommentThread != null) {
            bugCommentThread.stop();
            bugCommentThread = null;
        }
    }

    /**
     * Excutes the data generation for bug's components chart in a thread.
     *
     * @param dataChartGenerator
     */
    public static void generateBugComponentData(BugComponentDistributionData dataChartGenerator) {
        if (bugComponentThread == null) {
            bugComponentThread = new Thread(dataChartGenerator);
        }
        bugComponentThread.start();
    }

    /**
     * Excutes the stop BugComponent thread.
     */
    public static void stopBugComponentThread() {
        if (bugComponentThread != null) {
            bugComponentThread.stop();
            bugComponentThread = null;
        }
    }

    /**
     * Excutes the data generation for metric chart in a thread.
     *
     * @param dataChartGenerator
     */
    public static void generateDataMetricThread(MetricDistributionData dataChartGenerator) {
        if (metricThread == null) {
            metricThread = new Thread(dataChartGenerator);
        }
        metricThread.start();
    }

    /**
     * Excutes the stop metric thread.
     */
    public static void stopMetricThread() {
        if (metricThread != null) {
            metricThread.stop();
            metricThread = null;
        }
    }

}
