package com.unaj.project.service.impl;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.CursoDemandaDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Configuracion;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.MatriculaDetalle;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Pago;
import com.unaj.project.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AlumnoService alumnoService;
    private final CursoService cursoService;
    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final CicloService cicloService;
    private final ReporteService reporteService;
    private final ConfiguracionService configuracionService;

    public DashboardServiceImpl(AlumnoService alumnoService,
                                CursoService cursoService,
                                MatriculaService matriculaService,
                                PagoService pagoService,
                                CicloService cicloService,
                                ReporteService reporteService,
                                ConfiguracionService configuracionService) {
        this.alumnoService = alumnoService;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;
        this.pagoService = pagoService;
        this.cicloService = cicloService;
        this.reporteService = reporteService;
        this.configuracionService = configuracionService;
    }

    @Override
    public Map<String, Object> resumenInicio(Nivel nivel) {
        Configuracion configuracion = configuracionService.obtener();
        int cupoPorTurno = configuracion.getCupoPorTurno();
        int diasVencimientoProximo = configuracion.getDiasAvisoVencimiento();

        List<Matricula> matriculas = matriculaService.listarTodos();
        List<Pago> pagos = pagoService.listarTodos();

        long activas = matriculas.stream().filter(m -> "ACTIVA".equals(m.getEstado())).count();

        BigDecimal cobrado = pagoService.totalPorEstado(pagos, "PAGADO");
        BigDecimal pendiente = pagos.stream()
                .filter(p -> !"PAGADO".equals(p.getEstado()))
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        long vencidos = pagos.stream().filter(p -> "VENCIDO".equals(p.getEstado())).count();

        BigDecimal totalEmitido = cobrado.add(pendiente);
        BigDecimal tasaCobradoPct = totalEmitido.compareTo(BigDecimal.ZERO) > 0
                ? cobrado.divide(totalEmitido, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : null;
        long pagosPendientesCount = pagos.stream().filter(p -> !"PAGADO".equals(p.getEstado())).count();

        Map<String, Integer> aforo = new LinkedHashMap<>();
        aforo.put("Mañana", 0);
        aforo.put("Tarde", 0);
        aforo.put("Noche", 0);
        for (Matricula m : matriculas) {
            if ("ACTIVA".equals(m.getEstado()) && m.getTurno() != null) {
                aforo.merge(m.getTurno().getEtiqueta(), 1, Integer::sum);
            }
        }

        List<IngresoMensualDTO> ingresosPorMes = reporteService.ingresosPorMes();
        List<String> mesesLabels = ingresosPorMes.stream().map(IngresoMensualDTO::mes).toList();
        List<BigDecimal> mesesValores = ingresosPorMes.stream().map(IngresoMensualDTO::total).toList();

        BigDecimal ingresoMesActual = mesesValores.isEmpty()
                ? BigDecimal.ZERO : mesesValores.get(mesesValores.size() - 1);
        BigDecimal ingresoMesAnterior = mesesValores.size() < 2
                ? null : mesesValores.get(mesesValores.size() - 2);
        BigDecimal deltaIngresoPct = null;
        if (ingresoMesAnterior != null && ingresoMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            deltaIngresoPct = ingresoMesActual.subtract(ingresoMesAnterior)
                    .divide(ingresoMesAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        Map<String, Long> alumnosPorArea = alumnoService.contarPorArea(nivel);

        List<AlumnoMorosoDTO> morosos = reporteService.alumnosMorosos().stream().limit(5).toList();

        Map<Curso, Long> demandaPorCurso = new LinkedHashMap<>();
        for (Matricula m : matriculas) {
            if (!"ACTIVA".equals(m.getEstado())) continue;
            for (MatriculaDetalle d : m.getDetalles()) {
                if (d.getCurso() != null) {
                    demandaPorCurso.merge(d.getCurso(), 1L, Long::sum);
                }
            }
        }
        List<CursoDemandaDTO> cursosMasDemandados = demandaPorCurso.entrySet().stream()
                .sorted(Map.Entry.<Curso, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new CursoDemandaDTO(
                        e.getKey().getNombre(),
                        e.getKey().getProfesor() != null ? e.getKey().getProfesor().getNombreCompleto() : "Sin asignar",
                        e.getValue()))
                .toList();

        LocalDate hoy = LocalDate.now();
        LocalDate limiteProximo = hoy.plusDays(diasVencimientoProximo);
        List<Pago> proximosVencimientos = pagos.stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()) || "PARCIAL".equals(p.getEstado()))
                .filter(p -> p.getFechaVencimiento() != null && !p.getFechaVencimiento().isAfter(limiteProximo))
                .sorted(Comparator.comparing(Pago::getFechaVencimiento))
                .limit(5)
                .toList();

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("totalAlumnos", alumnoService.listarTodos().size());
        datos.put("totalCursos", cursoService.listarTodos().size());
        datos.put("totalMatriculas", activas);
        datos.put("cobrado", cobrado);
        datos.put("pendiente", pendiente);
        datos.put("vencidos", vencidos);
        datos.put("cicloActivo", cicloService.obtenerActivo());
        datos.put("ultimas", matriculas.stream().limit(5).toList());
        datos.put("aforo", aforo);
        datos.put("cupoPorTurno", cupoPorTurno);
        datos.put("mesesLabels", mesesLabels);
        datos.put("mesesValores", mesesValores);
        datos.put("ingresoMesActual", ingresoMesActual);
        datos.put("deltaIngresoPct", deltaIngresoPct);
        datos.put("alumnosPorArea", alumnosPorArea);
        datos.put("nivel", nivel);
        datos.put("niveles", Nivel.values());
        datos.put("morosos", morosos);
        datos.put("proximosVencimientos", proximosVencimientos);
        datos.put("hoy", hoy);
        datos.put("tasaCobradoPct", tasaCobradoPct);
        datos.put("pagosPendientesCount", pagosPendientesCount);
        datos.put("cursosMasDemandados", cursosMasDemandados);
        return datos;
    }
}