package com.unaj.project.service.impl;

import com.unaj.project.service.ChartImageService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
public class ChartImageServiceImpl implements ChartImageService {

    // Misma paleta que los gráficos Chart.js de las páginas web (ver CLAUDE.md: secuencia
    // categórica validada contra el validador CVD del skill de dataviz).
    private static final Color AZUL = new Color(0x25, 0x63, 0xEB);
    private static final Color AMBAR = new Color(0xD9, 0x77, 0x06);
    private static final Color CIELO = new Color(0x0E, 0xA5, 0xE9);
    private static final Color ROSA = new Color(0xDC, 0x26, 0x26);
    private static final Color LAVANDA = new Color(0x63, 0x66, 0xF1);
    private static final Color[] PALETA = {AZUL, AMBAR, CIELO, ROSA, LAVANDA};
    private static final Color TINTA_SUAVE = new Color(0x57, 0x68, 0x7D);
    private static final Color LINEA_SUAVE = new Color(0xEA, 0xF1, 0xF9);
    private static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 13);

    @Override
    public String barChart(List<String> etiquetas, List<Long> valores, String tituloSerie) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), tituloSerie, etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
        CategoryPlot plot = estilizarPlot(chart);
        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, AZUL);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.12);
        plot.setRenderer(renderer);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        return aDataUri(chart, 680, 320);
    }

    @Override
    public String horizontalBarChart(List<String> etiquetas, List<BigDecimal> valores, String tituloSerie) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), tituloSerie, etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset, PlotOrientation.HORIZONTAL, false, false, false);
        CategoryPlot plot = estilizarPlot(chart);
        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, ROSA);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.14);
        plot.setRenderer(renderer);
        return aDataUri(chart, 680, 320);
    }

    @Override
    public String lineChart(List<String> etiquetas, List<BigDecimal> valores, String tituloSerie) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), tituloSerie, etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
        CategoryPlot plot = estilizarPlot(chart);
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, AZUL);
        renderer.setSeriesStroke(0, new BasicStroke(2.4f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
        return aDataUri(chart, 680, 320);
    }

    @Override
    public String pieChart(List<String> etiquetas, List<Long> valores) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.setValue(etiquetas.get(i), valores.get(i));
        }
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, false, false);
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.04);
        List<String> claves = dataset.getKeys();
        for (int i = 0; i < claves.size(); i++) {
            plot.setSectionPaint(claves.get(i), PALETA[i % PALETA.length]);
        }
        chart.setBackgroundPaint(Color.WHITE);
        LegendTitle leyenda = chart.getLegend();
        if (leyenda != null) {
            leyenda.setItemFont(FUENTE);
        }
        return aDataUri(chart, 420, 320);
    }

    private CategoryPlot estilizarPlot(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(LINEA_SUAVE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        CategoryAxis ejeCategoria = plot.getDomainAxis();
        ejeCategoria.setLabelFont(FUENTE);
        ejeCategoria.setTickLabelFont(FUENTE);
        ejeCategoria.setTickLabelPaint(TINTA_SUAVE);
        NumberAxis ejeNumero = (NumberAxis) plot.getRangeAxis();
        ejeNumero.setLabelFont(FUENTE);
        ejeNumero.setTickLabelFont(FUENTE);
        ejeNumero.setTickLabelPaint(TINTA_SUAVE);
        return plot;
    }

    private String aDataUri(JFreeChart chart, int ancho, int alto) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(salida, chart, ancho, alto);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar la imagen del gráfico.", e);
        }
    }
}
