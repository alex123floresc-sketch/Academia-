package com.unaj.project.service.impl;

import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Pago;
import com.unaj.project.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int CUPO_POR_TURNO = 60;

    private final AlumnoService alumnoService;
    private final CursoService cursoService;
    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final CicloService cicloService;
    private final ReporteService reporteService;

    public DashboardServiceImpl(AlumnoService alumnoService,
                                CursoService cursoService,
                                MatriculaService matriculaService,
                                PagoService pagoService,
                                CicloService cicloService,
                                ReporteService reporteService) {
        this.alumnoService = alumnoService;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;
        this.pagoService = pagoService;
        this.cicloService = cicloService;
        this.reporteService = reporteService;
    }

    @Override
    public Map<String, Object> resumenInicio() {
        List<Matricula> matriculas = matriculaService.listarTodos();
        List<Pago> pagos = pagoService.listarTodos();

        long activas = matriculas.stream().filter(m -> "ACTIVA".equals(m.getEstado())).count();

        BigDecimal cobrado = pagoService.totalPorEstado(pagos, "PAGADO");
        BigDecimal pendiente = pagos.stream()
                .filter(p -> !"PAGADO".equals(p.getEstado()))
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        long vencidos = pagos.stream().filter(p -> "VENCIDO".equals(p.getEstado())).count();

        // Aforo por turno (matrículas activas)
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
        datos.put("cupoPorTurno", CUPO_POR_TURNO);
        datos.put("mesesLabels", mesesLabels);
        datos.put("mesesValores", mesesValores);
        return datos;
    }
}