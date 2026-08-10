package com.unaj.project.service;

import com.unaj.project.dto.FilaHorarioDTO;
import com.unaj.project.model.BloqueHorario;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.TipoBloque;
import com.unaj.project.model.Turno;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface HorarioService {

    Horario buscarPorId(Long id);

    BloqueHorario buscarBloque(Long id);

    void crearBloque(Long cicloId, Nivel nivel, Turno turno, LocalTime horaInicio, LocalTime horaFin, TipoBloque tipo, String area);

    void eliminarBloque(Long bloqueId);

    /** Bloques y cursos de un ciclo, nivel y area, agrupados por turno y ordenados por hora. Cada nivel+area tiene su propia grilla, independiente de las demas, aunque el ciclo se comparta entre niveles. */
    Map<Turno, List<FilaHorarioDTO>> agruparParaGrilla(Long cicloId, Nivel nivel, String area);

    /** Bloques horarios definidos por nivel, para el selector de nivel de /horarios (mismo patron que CursoService.contarPorNivel). */
    Map<Nivel, Long> contarBloquesPorNivel(Long cicloId);

    /** Bloques horarios definidos por area de un nivel, para el selector de area de /horarios (mismo patron que CursoService.contarPorArea). */
    Map<String, Long> contarBloquesPorArea(Long cicloId, Nivel nivel);

    void asignarCurso(Long bloqueId, DiaSemana dia, List<Long> cursoIds);

    void quitarCurso(Long horarioId);

    List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia);
}
