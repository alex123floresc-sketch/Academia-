package com.unaj.project.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Renderiza gráficos como imágenes PNG (data URI) para incrustar en los PDF de reportes.
 * Los gráficos de las páginas web usan Chart.js sobre &lt;canvas&gt;, pero el generador de PDF
 * (flying-saucer) no ejecuta JavaScript, así que ahí no sirven — esto genera el equivalente
 * como imagen real, en el servidor, con la misma paleta de colores.
 */
public interface ChartImageService {

    /** data:image/png;base64,... listo para un <img src="...">. */
    String barChart(List<String> etiquetas, List<Long> valores, String tituloSerie);

    String horizontalBarChart(List<String> etiquetas, List<BigDecimal> valores, String tituloSerie);

    String lineChart(List<String> etiquetas, List<BigDecimal> valores, String tituloSerie);

    String pieChart(List<String> etiquetas, List<Long> valores);
}
