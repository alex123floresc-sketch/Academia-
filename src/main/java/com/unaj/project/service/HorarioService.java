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

    Jornada buscarJornada(Long cicloId, DiaSemana dia, Turno turno);

    Map<DiaSemana, Map<Turno, Jornada>> agruparParaGrilla(Long cicloId);

    void guardarJornada(Long cicloId, DiaSemana dia, Turno turno, LocalTime horaInicio, LocalTime horaFin,
                        List<Long> cursoIds);

    void editarHoras(Long jornadaId, LocalTime horaInicio, LocalTime horaFin);

    void quitarCurso(Long horarioId);

    void eliminarJornada(Long jornadaId);

    List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia);
}
