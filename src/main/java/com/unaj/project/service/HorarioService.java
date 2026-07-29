package com.unaj.project.service;

import com.unaj.project.dto.FilaHorarioDTO;
import com.unaj.project.model.BloqueHorario;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.TipoBloque;
import com.unaj.project.model.Turno;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface HorarioService {

    Horario buscarPorId(Long id);

    BloqueHorario buscarBloque(Long id);

    void crearBloque(Long cicloId, Turno turno, LocalTime horaInicio, LocalTime horaFin, TipoBloque tipo);

    void eliminarBloque(Long bloqueId);

    /** Bloques y cursos asignados de un ciclo, agrupados por turno y ordenados por hora. */
    Map<Turno, List<FilaHorarioDTO>> agruparParaGrilla(Long cicloId);

    void asignarCurso(Long bloqueId, DiaSemana dia, List<Long> cursoIds);

    void quitarCurso(Long horarioId);

    List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia);
}
