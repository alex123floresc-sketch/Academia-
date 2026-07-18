package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.service.ReporteService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final TemplateEngine templateEngine;

    public ReporteController(ReporteService reporteService, TemplateEngine templateEngine) {
        this.reporteService = reporteService;
        this.templateEngine = templateEngine;
    }

    @GetMapping
    public String lista() {
        return "reportes/lista";
    }

    @GetMapping("/alumnos-por-ciclo/pdf")
    public ResponseEntity<byte[]> alumnosPorCicloPdf() throws Exception {
        Context context = new Context();
        context.setVariable("filas", reporteService.alumnosPorCicloTurno());
        return generarPdf("reportes/alumnos-por-ciclo-pdf", context, "alumnos_por_ciclo.pdf");
    }

    @GetMapping("/alumnos-por-ciclo/excel")
    public ResponseEntity<byte[]> alumnosPorCicloExcel() throws Exception {
        List<AlumnosPorCicloTurnoDTO> filas = reporteService.alumnosPorCicloTurno();
        return generarExcel("Alumnos por ciclo", new String[]{"Ciclo", "Turno", "Alumnos matriculados"},
                filas, fila -> new Object[]{fila.ciclo(), fila.turno(), fila.cantidad()},
                "alumnos_por_ciclo.xlsx");
    }

    @GetMapping("/ingresos-mensuales/pdf")
    public ResponseEntity<byte[]> ingresosMensualesPdf() throws Exception {
        Context context = new Context();
        context.setVariable("filas", reporteService.ingresosPorMes());
        return generarPdf("reportes/ingresos-mensuales-pdf", context, "ingresos_mensuales.pdf");
    }

    @GetMapping("/ingresos-mensuales/excel")
    public ResponseEntity<byte[]> ingresosMensualesExcel() throws Exception {
        List<IngresoMensualDTO> filas = reporteService.ingresosPorMes();
        return generarExcel("Ingresos por mes", new String[]{"Mes", "Total cobrado"},
                filas, fila -> new Object[]{fila.mes(), fila.total()},
                "ingresos_mensuales.xlsx");
    }

    @GetMapping("/morosos/pdf")
    public ResponseEntity<byte[]> morososPdf() throws Exception {
        Context context = new Context();
        context.setVariable("filas", reporteService.alumnosMorosos());
        return generarPdf("reportes/morosos-pdf", context, "alumnos_morosos.pdf");
    }

    @GetMapping("/morosos/excel")
    public ResponseEntity<byte[]> morososExcel() throws Exception {
        List<AlumnoMorosoDTO> filas = reporteService.alumnosMorosos();
        return generarExcel("Alumnos morosos", new String[]{"Nombre", "Apellido", "Correo", "Pagos vencidos", "Monto adeudado"},
                filas, fila -> new Object[]{fila.nombre(), fila.apellido(), fila.email(), fila.pagosVencidos(), fila.montoAdeudado()},
                "alumnos_morosos.xlsx");
    }

    private ResponseEntity<byte[]> generarPdf(String template, Context context, String filename) throws Exception {
        String html = templateEngine.process(template, context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        byte[] pdfBytes = outputStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }

    private <T> ResponseEntity<byte[]> generarExcel(String hoja, String[] encabezados, List<T> filas,
                                                     java.util.function.Function<T, Object[]> mapeoFila,
                                                     String filename) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(hoja);

            Row header = sheet.createRow(0);
            for (int i = 0; i < encabezados.length; i++) {
                header.createCell(i).setCellValue(encabezados[i]);
            }

            int rowIdx = 1;
            for (T fila : filas) {
                Row row = sheet.createRow(rowIdx++);
                Object[] valores = mapeoFila.apply(fila);
                for (int i = 0; i < valores.length; i++) {
                    Cell cell = row.createCell(i);
                    Object valor = valores[i];
                    if (valor instanceof Number numero) {
                        cell.setCellValue(numero.doubleValue());
                    } else {
                        cell.setCellValue(String.valueOf(valor));
                    }
                }
            }

            for (int i = 0; i < encabezados.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(bytes);
        }
    }
}
