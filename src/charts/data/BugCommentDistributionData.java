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

import charts.Chart;
import charts.XYSplineChart;
import dao.BugDAO;
import dao.DAOManager;
import dao.DAONameEnum;
import dto.TopicDTO;
import dto.VersionDTO;
import java.util.ArrayList;
import java.util.ResourceBundle;
import metrics.Metric;
import org.jfree.data.time.SimpleTimePeriod;

public class BugCommentDistributionData extends DataChartGenerator {

    public BugCommentDistributionData(Chart chart) {
        super(chart);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setDataSource(ArrayList<TopicDTO> topics, Metric metric) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void generateData() {
        int versionCount = (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getCount();
        chart.destroy();
        ((XYSplineChart) chart).setYLabel(ResourceBundle.getBundle("view/Bundle").getString("Chart2.YLabel"));
        ((XYSplineChart) chart).setTitle(ResourceBundle.getBundle("view/Bundle").getString("Chart2.Title"));
        ((XYSplineChart) chart).createNewSerie(1, ResourceBundle.getBundle("view/Bundle").getString("Chart2.Serie1"));
        ((XYSplineChart) chart).createNewSerie(2, ResourceBundle.getBundle("view/Bundle").getString("Chart2.Serie2"));
        ((XYSplineChart) chart).createNewSerie(3, ResourceBundle.getBundle("view/Bundle").getString("Chart2.Serie3"));
        ((XYSplineChart) chart).setDefaultXYItemRenderer();
        ((XYSplineChart) chart).setSeriesShapesVisible(0, false);
        ((XYSplineChart) chart).setSeriesShapesVisible(1, false);
        ((XYSplineChart) chart).setSeriesShapesVisible(2, false);
        for (int i = 1; i <= versionCount; i++) {
            VersionDTO version = (VersionDTO) (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).get(i);
            int bugCountVersionNotClosed = ((BugDAO) (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()))).getCountBeetwDatesClosed(new java.sql.Date(version.getDateFrom().getTime()), new java.sql.Date(version.getDateTo().getTime()), false);
            int bugCountVersionClosed = ((BugDAO) (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()))).getCountBeetwDatesClosed(new java.sql.Date(version.getDateFrom().getTime()), new java.sql.Date(version.getDateTo().getTime()), true);
            int commentCount = (DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName())).getCountBeetwDates(new java.sql.Date(version.getDateFrom().getTime()), new java.sql.Date(version.getDateTo().getTime()));
            ((XYSplineChart) chart).addItemToSerie(1, new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), bugCountVersionNotClosed);
            ((XYSplineChart) chart).addItemToSerie(2, new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), bugCountVersionClosed);
            ((XYSplineChart) chart).addItemToSerie(3, new SimpleTimePeriod(version.getDateFrom(), version.getDateTo()), commentCount);
        }
    }

    @Override
    public void run() {
        if (isReady()) {
            generateData();
        } else {
            setChanged();
            notifyObservers("NO TIEN DATOS BUG y comentario GENNN");
        }
    }

}
