package com.unaj.project.service.impl;

import com.unaj.project.dto.FilaHorarioDTO;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.*;
import com.unaj.project.repository.BloqueHorarioRepository;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.HorarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final CicloRepository cicloRepository;
    private final CursoRepository cursoRepository;

    public HorarioServiceImpl(HorarioRepository horarioRepository, BloqueHorarioRepository bloqueHorarioRepository,
                              CicloRepository cicloRepository, CursoRepository cursoRepository) {
        this.horarioRepository = horarioRepository;
        this.bloqueHorarioRepository = bloqueHorarioRepository;
        this.cicloRepository = cicloRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    public Horario buscarPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado (id " + id + ")."));
    }

    @Override
    public BloqueHorario buscarBloque(Long id) {
        return bloqueHorarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloque horario no encontrado (id " + id + ")."));
    }

    @Override
    @Transactional
    public void crearBloque(Long cicloId, Turno turno, LocalTime horaInicio, LocalTime horaFin, TipoBloque tipo) {
        if (horaInicio == null || horaFin == null || !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }
        if (bloqueHorarioRepository.existsByCicloIdAndTurnoAndHoraInicio(cicloId, turno, horaInicio)) {
            throw new IllegalArgumentException("Ya existe un bloque que inicia a esa hora en este turno.");
        }
        Ciclo ciclo = cicloRepository.findById(cicloId)
                .orElseThrow(() -> new IllegalArgumentException("Ciclo no encontrado: " + cicloId));

        BloqueHorario bloque = new BloqueHorario();
        bloque.setCiclo(ciclo);
        bloque.setTurno(turno);
        bloque.setHoraInicio(horaInicio);
        bloque.setHoraFin(horaFin);
        bloque.setTipo(tipo != null ? tipo : TipoBloque.CLASE);
        bloqueHorarioRepository.save(bloque);
    }

    @Override
    @Transactional
    public void eliminarBloque(Long bloqueId) {
        bloqueHorarioRepository.deleteById(bloqueId);
    }

    @Override
    public Map<Turno, List<FilaHorarioDTO>> agruparParaGrilla(Long cicloId) {
        Map<Turno, List<FilaHorarioDTO>> resultado = new LinkedHashMap<>();
        for (Turno t : Turno.values()) {
            resultado.put(t, new ArrayList<>());
        }
        if (cicloId == null) return resultado;

        List<BloqueHorario> bloques = bloqueHorarioRepository.findByCicloIdOrderByHoraInicioAsc(cicloId);
        List<Horario> horarios = horarioRepository.findByCicloId(cicloId);

        Map<Long, Map<DiaSemana, List<Horario>>> porBloque = new LinkedHashMap<>();
        for (Horario h : horarios) {
            porBloque.computeIfAbsent(h.getBloque().getId(), k -> new EnumMap<>(DiaSemana.class))
                    .computeIfAbsent(h.getDiaSemana(), k -> new ArrayList<>())
                    .add(h);
        }

        for (BloqueHorario bloque : bloques) {
            Map<DiaSemana, List<Horario>> porDia = porBloque.getOrDefault(bloque.getId(), Map.of());
            resultado.get(bloque.getTurno()).add(new FilaHorarioDTO(bloque, porDia));
        }
        return resultado;
    }

    @Override
    @Transactional
    public void asignarCurso(Long bloqueId, DiaSemana dia, List<Long> cursoIds) {
        BloqueHorario bloque = buscarBloque(bloqueId);
        if (bloque.isReceso()) {
            throw new IllegalArgumentException("No se pueden asignar cursos a un bloque de receso.");
        }
        if (cursoIds == null || cursoIds.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un curso.");
        }

        for (Long cursoId : cursoIds) {
            if (horarioRepository.existsByBloqueIdAndDiaSemanaAndCursoId(bloqueId, dia, cursoId)) {
                continue;
            }
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));
            Horario horario = new Horario();
            horario.setBloque(bloque);
            horario.setDiaSemana(dia);
            horario.setCurso(curso);
            horarioRepository.save(horario);
        }
    }

    @Override
    @Transactional
    public void quitarCurso(Long horarioId) {
        horarioRepository.deleteById(horarioId);
    }

    @Override
    public List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia) {
        if (cicloId == null || turno == null || dia == null) {
            return List.of();
        }
        return horarioRepository.findByCicloIdAndTurnoAndDiaSemana(cicloId, turno, dia);
    }
}
