package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorAreaDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Nivel;
import com.unaj.project.service.PdfGeneradorService;
import com.unaj.project.service.ReporteService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final PdfGeneradorService pdfGeneradorService;

    public ReporteController(ReporteService reporteService, PdfGeneradorService pdfGeneradorService) {
        this.reporteService = reporteService;
        this.pdfGeneradorService = pdfGeneradorService;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Nivel nivel, Model model) {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;

        List<AlumnosPorCicloTurnoDTO> porCiclo = reporteService.alumnosPorCicloTurno();
        List<IngresoMensualDTO> porMes = reporteService.ingresosPorMes();
        List<AlumnoMorosoDTO> morosos = reporteService.alumnosMorosos();
        List<AlumnosPorAreaDTO> porArea = reporteService.alumnosPorArea(nivelSel);

        long matriculasActivas = porCiclo.stream().mapToLong(AlumnosPorCicloTurnoDTO::cantidad).sum();
        String mesActual = YearMonth.now().toString();
        BigDecimal ingresosMesActual = porMes.stream()
                .filter(fila -> fila.mes().equals(mesActual))
                .map(IngresoMensualDTO::total)
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal montoAdeudado = morosos.stream()
                .map(AlumnoMorosoDTO::montoAdeudado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("porCiclo", porCiclo);
        model.addAttribute("porMes", porMes);
        model.addAttribute("morosos", morosos);
        model.addAttribute("porArea", porArea);
        model.addAttribute("matriculasActivas", matriculasActivas);
        model.addAttribute("ingresosMesActual", ingresosMesActual);
        model.addAttribute("montoAdeudado", montoAdeudado);
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("nivel", nivelSel);

        model.addAttribute("cicloTurnoLabels",
                porCiclo.stream().map(f -> f.ciclo() + " · " + f.turno()).toList());
        model.addAttribute("cicloTurnoValores",
                porCiclo.stream().map(AlumnosPorCicloTurnoDTO::cantidad).toList());
        model.addAttribute("mesLabels", porMes.stream().map(IngresoMensualDTO::mes).toList());
        model.addAttribute("mesValores", porMes.stream().map(IngresoMensualDTO::total).toList());
        model.addAttribute("areaLabels", porArea.stream().map(AlumnosPorAreaDTO::area).toList());
        model.addAttribute("areaValores", porArea.stream().map(AlumnosPorAreaDTO::cantidad).toList());
        List<AlumnoMorosoDTO> topMorosos = morosos.stream().limit(8).toList();
        model.addAttribute("morososLabels",
                topMorosos.stream().map(f -> f.nombre() + " " + f.apellido()).toList());
        model.addAttribute("morososValores", topMorosos.stream().map(AlumnoMorosoDTO::montoAdeudado).toList());

        return "reportes/lista";
    }

    @GetMapping("/alumnos-por-ciclo/pdf")
    public ResponseEntity<byte[]> alumnosPorCicloPdf() throws Exception {
        List<AlumnosPorCicloTurnoDTO> filas = reporteService.alumnosPorCicloTurno();
        long totalAlumnos = filas.stream().mapToLong(AlumnosPorCicloTurnoDTO::cantidad).sum();
        long totalCiclos = filas.stream().map(AlumnosPorCicloTurnoDTO::ciclo).distinct().count();
        long totalTurnos = filas.stream().map(AlumnosPorCicloTurnoDTO::turno).distinct().count();

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnos", totalAlumnos);
        context.setVariable("totalCiclos", totalCiclos);
        context.setVariable("totalTurnos", totalTurnos);
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
        List<IngresoMensualDTO> filas = reporteService.ingresosPorMes();
        BigDecimal totalAcumulado = filas.stream()
                .map(IngresoMensualDTO::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedioMensual = filas.isEmpty() ? BigDecimal.ZERO
                : totalAcumulado.divide(BigDecimal.valueOf(filas.size()), 2, RoundingMode.HALF_UP);
        IngresoMensualDTO mesMayor = filas.stream().max(Comparator.comparing(IngresoMensualDTO::total)).orElse(null);
        IngresoMensualDTO mesMenor = filas.stream().min(Comparator.comparing(IngresoMensualDTO::total)).orElse(null);

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAcumulado", totalAcumulado);
        context.setVariable("promedioMensual", promedioMensual);
        context.setVariable("mesMayor", mesMayor);
        context.setVariable("mesMenor", mesMenor);
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
        List<AlumnoMorosoDTO> filas = reporteService.alumnosMorosos();
        long totalPagosVencidos = filas.stream().mapToLong(AlumnoMorosoDTO::pagosVencidos).sum();
        BigDecimal totalAdeudado = filas.stream()
                .map(AlumnoMorosoDTO::montoAdeudado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedioAdeudado = filas.isEmpty() ? BigDecimal.ZERO
                : totalAdeudado.divide(BigDecimal.valueOf(filas.size()), 2, RoundingMode.HALF_UP);

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnosMorosos", (long) filas.size());
        context.setVariable("totalPagosVencidos", totalPagosVencidos);
        context.setVariable("totalAdeudado", totalAdeudado);
        context.setVariable("promedioAdeudado", promedioAdeudado);
        return generarPdf("reportes/morosos-pdf", context, "alumnos_morosos.pdf");
    }

    @GetMapping("/morosos/excel")
    public ResponseEntity<byte[]> morososExcel() throws Exception {
        List<AlumnoMorosoDTO> filas = reporteService.alumnosMorosos();
        return generarExcel("Alumnos morosos", new String[]{"Nombre", "Apellido", "Correo", "Pagos vencidos", "Monto adeudado"},
                filas, fila -> new Object[]{fila.nombre(), fila.apellido(), fila.email(), fila.pagosVencidos(), fila.montoAdeudado()},
                "alumnos_morosos.xlsx");
    }

    @GetMapping("/alumnos-por-area/pdf")
    public ResponseEntity<byte[]> alumnosPorAreaPdf(@RequestParam(required = false) Nivel nivel) throws Exception {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;
        List<AlumnosPorAreaDTO> filas = reporteService.alumnosPorArea(nivelSel);
        long totalAlumnos = filas.stream().mapToLong(AlumnosPorAreaDTO::cantidad).sum();

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnos", totalAlumnos);
        context.setVariable("nivel", nivelSel);
        return generarPdf("reportes/alumnos-por-area-pdf", context, "alumnos_por_area_" + nivelSel.name() + ".pdf");
    }

    @GetMapping("/alumnos-por-area/excel")
    public ResponseEntity<byte[]> alumnosPorAreaExcel(@RequestParam(required = false) Nivel nivel) throws Exception {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;
        List<AlumnosPorAreaDTO> filas = reporteService.alumnosPorArea(nivelSel);
        return generarExcel("Área - " + nivelSel.getEtiqueta(), new String[]{"Área", "Alumnos"},
                filas, fila -> new Object[]{fila.area(), fila.cantidad()},
                "alumnos_por_area_" + nivelSel.name() + ".xlsx");
    }

    private ResponseEntity<byte[]> generarPdf(String template, Context context, String filename) throws Exception {
        context.setVariable("fechaGeneracion", LocalDate.now());
        byte[] pdfBytes = pdfGeneradorService.renderizar(template, context);
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
