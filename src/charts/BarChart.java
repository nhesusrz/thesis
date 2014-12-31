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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChart extends Chart {

    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private final DefaultCategoryDataset dataSet;

    public BarChart() {
        dataSet = new DefaultCategoryDataset();
        chart = createChart(dataSet);
        chartPanel = new ChartPanel(chart);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
    }

    public void addValue(Double value, String component) {
        dataSet.addValue(value, "Category", component);
    }

    @Override
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    @Override
    public void setYLabel(String label) {
        chart.getCategoryPlot().getRangeAxis().setLabel(label);
    }

    /**
     * Sets the label for the domanin axis
     *
     * @param label Text for label.
     */
    public void setXLabel(String label) {
        chart.getCategoryPlot().getDomainAxis().setLabel(label);
    }

    @Override
    public void setPreferredSize(int x, int y) {
        chartPanel.setPreferredSize(new Dimension(x, y));
    }

    @Override
    public ChartPanel getGraphicPanel() {
        chartPanel.setPreferredSize(new Dimension(380, 380));
        return chartPanel;
    }

    @Override
    public void destroy() {
        dataSet.clear();
    }

    private static JFreeChart createChart(DefaultCategoryDataset dataSet) {
        JFreeChart chart = ChartFactory.createBarChart(
            ResourceBundle.getBundle("view/Bundle").getString("Chart.Title"),
            ResourceBundle.getBundle("view/Bundle").getString("Chart.YLabel"),
            ResourceBundle.getBundle("view/Bundle").getString("Chart.XLabel"),
            dataSet,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
//        chart.getCategoryPlot().getRenderer().setBaseItemLabelsVisible(true);
//        chart.getCategoryPlot().getRenderer().setBaseSeriesVisibleInLegend(false);
//        chart.setBackgroundPaint(Color.white);
//        chart.getCategoryPlot().setBackgroundPaint(Color.white);
//        chart.getCategoryPlot().setDomainGridlinePaint(Color.lightGray);
//        chart.getCategoryPlot().setRangeGridlinePaint(Color.lightGray);
//        chart.getCategoryPlot().setOutlinePaint(Color.white);
//        chart.getCategoryPlot().getRangeAxis().setAutoRange(true);
        return chart;
    }

}
