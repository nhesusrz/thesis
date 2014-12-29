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
package charts;

import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class PieChart extends Chart {

    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private final DefaultPieDataset dataSet;

    public PieChart() {
        dataSet = new DefaultPieDataset();
        chart = createChart(dataSet);
        chartPanel = new ChartPanel(chart);
    }

    /**
     * Adds a value to the data set.
     *
     * @param value Is the value.
     * @param component Is the type of compenent related to a bug.
     */
    public void addValue(Double value, String component) {
        dataSet.setValue(component, value);
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setYLabel(String label) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        dataSet.clear();
    }

    private static JFreeChart createChart(PieDataset dataSet) {
        JFreeChart chart = ChartFactory.createPieChart(
                ResourceBundle.getBundle("view/Bundle").getString("Chart1.Title"),
                dataSet,
                true,
                true,
                false
        );
        chart.setBackgroundPaint(Color.white);
        final PiePlot plot = (PiePlot) chart.getPlot();
        plot.setNoDataMessage(ResourceBundle.getBundle("view/Bundle").getString("Chart.NoData"));
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.white);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}= {2}", NumberFormat.getNumberInstance(), new DecimalFormat("0.00%")));
        return chart;
    }

    @Override
    public void setPreferredSize(int x, int y) {
        chartPanel.setPreferredSize(new Dimension(x, y));
    }

    @Override
    public ChartPanel getGraphicPanel() {
        return chartPanel;
    }
}
