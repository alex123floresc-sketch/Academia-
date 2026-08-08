package com.unaj.project.service.impl;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorAreaDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Turno;
import com.unaj.project.repository.AbonoRepository;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.service.AlumnoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReporteServiceImpl implements com.unaj.project.service.ReporteService {

    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pagoRepository;
    private final AbonoRepository abonoRepository;
    private final AlumnoService alumnoService;

    public ReporteServiceImpl(MatriculaRepository matriculaRepository, PagoRepository pagoRepository,
                              AbonoRepository abonoRepository, AlumnoService alumnoService) {
        this.matriculaRepository = matriculaRepository;
        this.pagoRepository = pagoRepository;
        this.abonoRepository = abonoRepository;
        this.alumnoService = alumnoService;
    }

    @Override
    public List<AlumnosPorCicloTurnoDTO> alumnosPorCicloTurno() {
        return matriculaRepository.contarAlumnosPorCicloYTurno().stream()
                .map(fila -> new AlumnosPorCicloTurnoDTO(
                        (String) fila[0],
                        ((Turno) fila[1]).getEtiqueta(),
                        (Long) fila[2]))
                .toList();
    }

    @Override
    public List<IngresoMensualDTO> ingresosPorMes() {
        return abonoRepository.sumarPorMes().stream()
                .map(fila -> new IngresoMensualDTO(
                        (String) fila[0],
                        (BigDecimal) fila[1]))
                .toList();
    }

    @Override
    public List<AlumnosPorAreaDTO> alumnosPorArea(Nivel nivel) {
        return alumnoService.contarPorArea(nivel).entrySet().stream()
                .map(e -> new AlumnosPorAreaDTO(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<AlumnoMorosoDTO> alumnosMorosos() {
        return pagoRepository.listarMorosos().stream()
                .map(fila -> new AlumnoMorosoDTO(
                        (Long) fila[0],
                        (String) fila[1],
                        (String) fila[2],
                        (String) fila[3],
                        (Long) fila[4],
                        (BigDecimal) fila[5]))
                .toList();
    }
}
