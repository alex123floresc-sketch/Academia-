package com.unaj.project.service;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorAreaDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.AlumnosPorNivelDTO;
import com.unaj.project.dto.IngresoMensualDTO;
import com.unaj.project.model.Nivel;

import java.util.List;

public interface ReporteService {
    /** Matrículas activas por ciclo y turno, en todos los niveles (el ciclo se comparte entre niveles). */
    List<AlumnosPorCicloTurnoDTO> alumnosPorCicloTurno();

    /** Igual que alumnosPorCicloTurno(), acotado a un solo nivel — lo que usa /reportes?nivel=X. */
    List<AlumnosPorCicloTurnoDTO> alumnosPorCicloTurno(Nivel nivel);

    /** Abonos cobrados por mes, en todos los niveles. */
    List<IngresoMensualDTO> ingresosPorMes();

    /** Igual que ingresosPorMes(), acotado a un solo nivel. */
    List<IngresoMensualDTO> ingresosPorMes(Nivel nivel);

    /** Alumnos con pagos vencidos, en todos los niveles. */
    List<AlumnoMorosoDTO> alumnosMorosos();

    /** Igual que alumnosMorosos(), acotado a un solo nivel. */
    List<AlumnoMorosoDTO> alumnosMorosos(Nivel nivel);

    /** Distribución de alumnos por área/grado de un nivel específico — este reporte sí es por nivel. */
    List<AlumnosPorAreaDTO> alumnosPorArea(Nivel nivel);

    /** Distribución de alumnos por nivel (Primaria/Secundaria/Preuniversitario), en un solo reporte comparativo. */
    List<AlumnosPorNivelDTO> alumnosPorNivel();
}
