package com.unaj.project.service;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.dto.AsistenciaResumenAlumnoDTO;
import com.unaj.project.model.Asistencia;

import java.util.List;

public interface AsistenciaService {

    List<Asistencia> listarDeHoy(Long horarioId);

    long contarDeHoy(Long horarioId);

    AsistenciaResultadoDTO registrar(Long horarioId, String codigoQr, String username);

    /**
     * Resumen de asistencias/faltas/tardanzas de un alumno para un ciclo, con calendario mes a
     * mes. Si cicloId es null, usa la matrícula más reciente del alumno. Si el alumno no tiene
     * matrícula en ese ciclo, o no tiene horarios asignados, devuelve un resumen vacío (no lanza
     * excepción) para que la vista lo muestre sin datos en vez de romperse.
     */
    AsistenciaResumenAlumnoDTO resumenPorAlumno(Long alumnoId, Long cicloId);
}
