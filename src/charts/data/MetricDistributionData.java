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

import charts.XYSplineChart;
import dao.DAOManager;
import dao.DAONameEnum;
import dto.TopicDTO;
import dto.VersionDTO;
import java.util.ArrayList;
import java.util.ResourceBundle;
import metrics.Metric;
import org.jfree.data.time.SimpleTimePeriod;

public class MetricDistributionData extends DataChartGenerator {

    private ArrayList<TopicDTO> topics;
    private Metric metric;

    public MetricDistributionData(XYSplineChart chart) {
        super(chart);
        topics = null;
        metric = null;
    }

    @Override
    public boolean isReady() {
        if (metric != null && topics != null && topics.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void setDataSource(ArrayList<TopicDTO> topics, Metric metric) {
        this.topics = topics;
        this.metric = metric;
    }

    @Override
    protected void generateData() {
        synchronized(chart) {
            if (!topics.isEmpty()) {
            int versionCount = (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getCount();
            chart.destroy();
            ((XYSplineChart) chart).setYLabel(metric.toString());
            ((XYSplineChart) chart).setTitle(metric.toString() + " Evolution");
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Metric.Action.Inic"));
            for (int versionId = 1; versionId <= versionCount; versionId++) {
                VersionDTO version = (VersionDTO) (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).get(versionId);
                for (TopicDTO topic : topics) {
                    ((XYSplineChart) chart).createNewSerie(topic.getID(), topic.toStringWithoutHTMLTags());
                    switch (metric.getCode()) {
                        case 1:
                            ((XYSplineChart) chart).addItemToSerie(topic.getID(), new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), metric.getResult(version, topic).doubleValue());
                            break;
                        case 2:
                            ((XYSplineChart) chart).addItemToSerie(topic.getID(), new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), metric.getResult(version, topic).doubleValue());
                            break;
                        case 3:
                            ((XYSplineChart) chart).addItemToSerie(topic.getID(), new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), metric.getResult(version, topic).doubleValue());
                            break;
                        case 4:
                            ((XYSplineChart) chart).addItemToSerie(topic.getID(), new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), metric.getResult(version, topic).doubleValue());
                            break;
                    }
                }
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Metric.Action.Run"));
            }
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("Metric.Action.End"));
            }
        }         
    }

    @Override
    public void run() {
        if (isReady()) {
            generateData();
        } 
    }


}
