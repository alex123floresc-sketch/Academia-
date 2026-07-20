// src/main/java/com/unaj/project/service/HorarioService.java
package com.unaj.project.service;

import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Jornada;
import com.unaj.project.model.Turno;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface HorarioService {

    Horario buscarPorId(Long id);

    /** Jornada de un ciclo+día+turno, o null si todavía no se creó ninguna. */
    Jornada buscarJornada(Long cicloId, DiaSemana dia, Turno turno);

    /** Todas las jornadas de un ciclo, listas para pintar la grilla completa (6 días x 3 turnos). */
    Map<DiaSemana, Map<Turno, Jornada>> agruparParaGrilla(Long cicloId);

    /**
     * Busca la jornada de ese ciclo+día+turno o la crea (validando horaFin > horaInicio), y le
     * agrega los cursos indicados que todavía no estén en ella.
     */
    void guardarJornada(Long cicloId, DiaSemana dia, Turno turno, LocalTime horaInicio, LocalTime horaFin,
                        List<Long> cursoIds);

    /** Actualiza el rango de horas de una jornada ya existente. */
    void editarHoras(Long jornadaId, LocalTime horaInicio, LocalTime horaFin);

    /** Quita un curso de su jornada. */
    void quitarCurso(Long horarioId);

    /** Elimina la jornada completa junto con todos sus cursos. */
    void eliminarJornada(Long jornadaId);

    /** Sesiones de un ciclo+turno+día específico (curso ya cargado), usado por Asistencia. */
    List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia);
}
