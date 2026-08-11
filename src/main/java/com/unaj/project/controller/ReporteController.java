package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorAreaDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.AlumnosPorNivelDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Nivel;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.ChartImageService;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.PdfGeneradorService;
import com.unaj.project.service.ProfesorService;
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
    private final AlumnoService alumnoService;
    private final CursoService cursoService;
    private final ProfesorService profesorService;
    private final PdfGeneradorService pdfGeneradorService;
    private final ChartImageService chartImageService;
    private final ConfiguracionService configuracionService;

    public ReporteController(ReporteService reporteService, AlumnoService alumnoService, CursoService cursoService,
                             ProfesorService profesorService, PdfGeneradorService pdfGeneradorService,
                             ChartImageService chartImageService, ConfiguracionService configuracionService) {
        this.reporteService = reporteService;
        this.alumnoService = alumnoService;
        this.cursoService = cursoService;
        this.profesorService = profesorService;
        this.pdfGeneradorService = pdfGeneradorService;
        this.chartImageService = chartImageService;
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Nivel nivel, Model model) {
        if (nivel == null) {
            model.addAttribute("resumenNiveles", alumnoService.contarPorNivel());
            return "reportes/niveles";
        }

        List<AlumnosPorCicloTurnoDTO> porCiclo = reporteService.alumnosPorCicloTurno(nivel);
        List<IngresoMensualDTO> porMes = reporteService.ingresosPorMes(nivel);
        List<AlumnoMorosoDTO> morosos = reporteService.alumnosMorosos(nivel);
        List<AlumnosPorAreaDTO> porArea = reporteService.alumnosPorArea(nivel);

        long matriculasActivas = porCiclo.stream().mapToLong(AlumnosPorCicloTurnoDTO::cantidad).sum();
        String mesActual = YearMonth.now().toString();
        BigDecimal ingresosMesActual = porMes.stream()
                .filter(fila -> fila.mes().equals(mesActual))
                .map(IngresoMensualDTO::total)
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal montoAdeudado = morosos.stream()
                .map(AlumnoMorosoDTO::montoAdeudado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAlumnosNivel = porArea.stream().mapToLong(AlumnosPorAreaDTO::cantidad).sum();
        BigDecimal tasaMorosidadPct = totalAlumnosNivel > 0
                ? BigDecimal.valueOf(morosos.size()).divide(BigDecimal.valueOf(totalAlumnosNivel), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        long totalCursosNivel = cursoService.contarPorNivel().getOrDefault(nivel, 0L);
        long totalProfesoresNivel = profesorService.listarTodos().stream()
                .filter(p -> p.getNiveles() != null && p.getNiveles().contains(nivel))
                .count();

        model.addAttribute("porCiclo", porCiclo);
        model.addAttribute("porMes", porMes);
        model.addAttribute("morosos", morosos);
        model.addAttribute("porArea", porArea);
        model.addAttribute("matriculasActivas", matriculasActivas);
        model.addAttribute("ingresosMesActual", ingresosMesActual);
        model.addAttribute("montoAdeudado", montoAdeudado);
        model.addAttribute("tasaMorosidadPct", tasaMorosidadPct);
        model.addAttribute("totalCursosNivel", totalCursosNivel);
        model.addAttribute("totalProfesoresNivel", totalProfesoresNivel);
        model.addAttribute("nivel", nivel);

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
    public ResponseEntity<byte[]> alumnosPorCicloPdf(@RequestParam Nivel nivel) throws Exception {
        List<AlumnosPorCicloTurnoDTO> filas = reporteService.alumnosPorCicloTurno(nivel);
        long totalAlumnos = filas.stream().mapToLong(AlumnosPorCicloTurnoDTO::cantidad).sum();
        long totalCiclos = filas.stream().map(AlumnosPorCicloTurnoDTO::ciclo).distinct().count();
        long totalTurnos = filas.stream().map(AlumnosPorCicloTurnoDTO::turno).distinct().count();

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnos", totalAlumnos);
        context.setVariable("totalCiclos", totalCiclos);
        context.setVariable("totalTurnos", totalTurnos);
        context.setVariable("nivel", nivel);
        if (!filas.isEmpty()) {
            context.setVariable("chartDataUri", chartImageService.barChart(
                    filas.stream().map(f -> f.ciclo() + " · " + f.turno()).toList(),
                    filas.stream().map(AlumnosPorCicloTurnoDTO::cantidad).toList(),
                    "Alumnos matriculados"));
        }
        return generarPdf("reportes/alumnos-por-ciclo-pdf", context, "alumnos_por_ciclo_" + nivel.name() + ".pdf");
    }

    @GetMapping("/alumnos-por-ciclo/excel")
    public ResponseEntity<byte[]> alumnosPorCicloExcel(@RequestParam Nivel nivel) throws Exception {
        List<AlumnosPorCicloTurnoDTO> filas = reporteService.alumnosPorCicloTurno(nivel);
        return generarExcel("Alumnos por ciclo", new String[]{"Ciclo", "Turno", "Alumnos matriculados"},
                filas, fila -> new Object[]{fila.ciclo(), fila.turno(), fila.cantidad()},
                "alumnos_por_ciclo_" + nivel.name() + ".xlsx");
    }

    @GetMapping("/ingresos-mensuales/pdf")
    public ResponseEntity<byte[]> ingresosMensualesPdf(@RequestParam Nivel nivel) throws Exception {
        List<IngresoMensualDTO> filas = reporteService.ingresosPorMes(nivel);
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
        context.setVariable("nivel", nivel);
        if (!filas.isEmpty()) {
            context.setVariable("chartDataUri", chartImageService.lineChart(
                    filas.stream().map(IngresoMensualDTO::mes).toList(),
                    filas.stream().map(IngresoMensualDTO::total).toList(),
                    "Ingresos (" + configuracionService.obtener().getSimboloMoneda() + ")"));
        }
        return generarPdf("reportes/ingresos-mensuales-pdf", context, "ingresos_mensuales_" + nivel.name() + ".pdf");
    }

    @GetMapping("/ingresos-mensuales/excel")
    public ResponseEntity<byte[]> ingresosMensualesExcel(@RequestParam Nivel nivel) throws Exception {
        List<IngresoMensualDTO> filas = reporteService.ingresosPorMes(nivel);
        return generarExcel("Ingresos por mes", new String[]{"Mes", "Total cobrado"},
                filas, fila -> new Object[]{fila.mes(), fila.total()},
                "ingresos_mensuales_" + nivel.name() + ".xlsx");
    }

    @GetMapping("/morosos/pdf")
    public ResponseEntity<byte[]> morososPdf(@RequestParam Nivel nivel) throws Exception {
        List<AlumnoMorosoDTO> filas = reporteService.alumnosMorosos(nivel);
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
        context.setVariable("nivel", nivel);
        List<AlumnoMorosoDTO> topMorosos = filas.stream().limit(10).toList();
        if (!topMorosos.isEmpty()) {
            context.setVariable("chartDataUri", chartImageService.horizontalBarChart(
                    topMorosos.stream().map(f -> f.nombre() + " " + f.apellido()).toList(),
                    topMorosos.stream().map(AlumnoMorosoDTO::montoAdeudado).toList(),
                    "Monto adeudado (" + configuracionService.obtener().getSimboloMoneda() + ")"));
        }
        return generarPdf("reportes/morosos-pdf", context, "alumnos_morosos_" + nivel.name() + ".pdf");
    }

    @GetMapping("/morosos/excel")
    public ResponseEntity<byte[]> morososExcel(@RequestParam Nivel nivel) throws Exception {
        List<AlumnoMorosoDTO> filas = reporteService.alumnosMorosos(nivel);
        return generarExcel("Alumnos morosos", new String[]{"Nombre", "Apellido", "Correo", "Pagos vencidos", "Monto adeudado"},
                filas, fila -> new Object[]{fila.nombre(), fila.apellido(), fila.email(), fila.pagosVencidos(), fila.montoAdeudado()},
                "alumnos_morosos_" + nivel.name() + ".xlsx");
    }

    @GetMapping("/alumnos-por-area/pdf")
    public ResponseEntity<byte[]> alumnosPorAreaPdf(@RequestParam Nivel nivel) throws Exception {
        List<AlumnosPorAreaDTO> filas = reporteService.alumnosPorArea(nivel);
        long totalAlumnos = filas.stream().mapToLong(AlumnosPorAreaDTO::cantidad).sum();

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnos", totalAlumnos);
        context.setVariable("nivel", nivel);
        if (!filas.isEmpty()) {
            context.setVariable("chartDataUri", chartImageService.pieChart(
                    filas.stream().map(AlumnosPorAreaDTO::area).toList(),
                    filas.stream().map(AlumnosPorAreaDTO::cantidad).toList()));
        }
        return generarPdf("reportes/alumnos-por-area-pdf", context, "alumnos_por_area_" + nivel.name() + ".pdf");
    }

    @GetMapping("/alumnos-por-area/excel")
    public ResponseEntity<byte[]> alumnosPorAreaExcel(@RequestParam Nivel nivel) throws Exception {
        List<AlumnosPorAreaDTO> filas = reporteService.alumnosPorArea(nivel);
        return generarExcel("Área - " + nivel.getEtiqueta(), new String[]{"Área", "Alumnos"},
                filas, fila -> new Object[]{fila.area(), fila.cantidad()},
                "alumnos_por_area_" + nivel.name() + ".xlsx");
    }

    @GetMapping("/alumnos-por-nivel/pdf")
    public ResponseEntity<byte[]> alumnosPorNivelPdf() throws Exception {
        List<AlumnosPorNivelDTO> filas = reporteService.alumnosPorNivel();
        long totalAlumnos = filas.stream().mapToLong(AlumnosPorNivelDTO::cantidad).sum();

        Context context = new Context();
        context.setVariable("filas", filas);
        context.setVariable("totalAlumnos", totalAlumnos);
        if (!filas.isEmpty()) {
            context.setVariable("chartDataUri", chartImageService.pieChart(
                    filas.stream().map(AlumnosPorNivelDTO::nivel).toList(),
                    filas.stream().map(AlumnosPorNivelDTO::cantidad).toList()));
        }
        return generarPdf("reportes/alumnos-por-nivel-pdf", context, "alumnos_por_nivel.pdf");
    }

    @GetMapping("/alumnos-por-nivel/excel")
    public ResponseEntity<byte[]> alumnosPorNivelExcel() throws Exception {
        List<AlumnosPorNivelDTO> filas = reporteService.alumnosPorNivel();
        return generarExcel("Alumnos por nivel", new String[]{"Nivel", "Alumnos"},
                filas, fila -> new Object[]{fila.nivel(), fila.cantidad()},
                "alumnos_por_nivel.xlsx");
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
