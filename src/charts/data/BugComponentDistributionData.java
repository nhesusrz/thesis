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
import charts.PieChart;
import dao.BugDAO;
import dao.DAOManager;
import dao.DAONameEnum;
import dto.TopicDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import metrics.Metric;

public class BugComponentDistributionData extends DataChartGenerator {

    public BugComponentDistributionData(Chart chart) {
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
        int bugCount = (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName())).getCount();
        HashMap<String, Integer> distribution = ((BugDAO) (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()))).getDistributionComponent();
        chart.destroy();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            ((PieChart) chart).addValue(new Double(entry.getValue()) / bugCount, entry.getKey());
        }
    }

    @Override
    public void run() {
        if (isReady()) {
            generateData();
        } else {
            setChanged();
            notifyObservers("NO TIEN DATOS BUG COMPONENTE GENNN");
        }
    }

}
