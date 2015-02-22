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
import java.awt.Stroke;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

public class XYSplineChart extends Chart {

    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private final TimePeriodValuesCollection dataSet;
    private final HashMap<Integer, TimePeriodValues> series;

    public XYSplineChart() {
        dataSet = new TimePeriodValuesCollection();
        chart = createChart(dataSet);
        chartPanel = new ChartPanel(chart);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        series = new HashMap<Integer, TimePeriodValues>();
    }

    /**
     * Creates a new serie in the chart.
     *
     * @param serieKey Is the key name for the serie.
     * @param name Is the name of the serie.
     */
    public void createNewSerie(Integer serieKey, String name) {
        synchronized (series) {
            if (!series.containsKey(serieKey)) {
                TimePeriodValues newSerie = new TimePeriodValues(name);
                dataSet.addSeries(newSerie);
                series.put(serieKey, newSerie);
            }
        }
    }

    /**
     * Adds a value to a serie.
     *
     * @param serieId Serie identificator.
     * @param x X point.
     * @param y Y point.
     */
    public void addItemToSerie(Integer serieId, SimpleTimePeriod x, double y) {
        synchronized (series) {
            series.get(serieId).add(x, y);
        }
    }

    @Override
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    @Override
    public void setYLabel(String label) {
        chart.getXYPlot().getRangeAxis().setLabel(label);
    }

    @Override
    public void destroy() {
        synchronized (dataSet) {
            for (Map.Entry<Integer, TimePeriodValues> entry : series.entrySet()) {
                dataSet.removeSeries(entry.getValue());
            }
            series.clear();
        }
    }

    /**
     * Sets the view of the line chart.
     */
    public void setLineRendererDotted() {
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
            Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 80.5f, new float[]{5.0f}, 80.0f);

            @Override
            public Stroke getItemStroke(int row, int column) {
                return stroke;
            }
        };;
        chart.getXYPlot().setRenderer(renderer);
    }

    /**
     * Sets the labels in the view chart.
     *
     * @param show The flag.
     */
    public void showLabels(boolean show) {
        final ItemLabelPosition p = new ItemLabelPosition(
            ItemLabelAnchor.OUTSIDE1, TextAnchor.TOP_LEFT, 
            TextAnchor.TOP_LEFT, -Math.PI / 8.0
        );
        chart.getXYPlot().getRenderer().setPositiveItemLabelPosition(p);
        chart.getXYPlot().getRenderer().setBaseItemLabelsVisible(show);
    }

    /**
     * Sets a default renderer for the primary dataset and sends a
     * PlotChangeEvent to all registered listeners. If the renderer is set to
     * null, no data will be displayed.
     *
     */
    public void setDefaultXYItemRenderer() {
        chart.getXYPlot().setRenderer(new DefaultXYItemRenderer());
    }

    /**
     * Sets the 'shapes visible' flag for a series and sends a
     * RendererChangeEvent to all registered listeners.
     *
     * @param serie The series index (zero-based).
     * @param flag The flag.
     */
    public void setSeriesShapesVisible(int serie, boolean flag) {
        ((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).setSeriesShapesVisible(serie, flag);
    }

    private JFreeChart createChart(TimePeriodValuesCollection dataSet) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                ResourceBundle.getBundle("view/Bundle").getString("Chart3.Title"),
                ResourceBundle.getBundle("view/Bundle").getString("Chart3.XLabel"),
                ResourceBundle.getBundle("view/Bundle").getString("Chart3.YLabel"),
                dataSet,
                //PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        chart.setBackgroundPaint(Color.white);
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
            Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 80.5f, new float[]{5.0f}, 80.0f);

            @Override
            public Stroke getItemStroke(int row, int column) {
                return stroke;
            }
        };;

        XYItemLabelGenerator xy = new StandardXYItemLabelGenerator("({1},{2})", new SimpleDateFormat("d/MM/yy"), new DecimalFormat("0.00"));

        renderer.setBaseItemLabelGenerator(xy);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseLinesVisible(true);
        renderer.setBaseItemLabelsVisible(false);
        
                
        chart.getXYPlot().setDomainZeroBaselineVisible(true);
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.white);
        chart.getXYPlot().setRangeGridlinePaint(Color.lightGray);
        chart.getXYPlot().setOutlinePaint(Color.white);
        chart.getXYPlot().setNoDataMessage(ResourceBundle.getBundle("view/Bundle").getString("Chart.NoData"));
        DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM-YY"));
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
