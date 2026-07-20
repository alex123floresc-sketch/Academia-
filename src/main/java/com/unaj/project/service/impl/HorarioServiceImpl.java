package com.unaj.project.service.impl;

import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.*;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.repository.JornadaRepository;
import com.unaj.project.service.HorarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final JornadaRepository jornadaRepository;
    private final CicloRepository cicloRepository;
    private final CursoRepository cursoRepository;

    public HorarioServiceImpl(HorarioRepository horarioRepository, JornadaRepository jornadaRepository,
                              CicloRepository cicloRepository, CursoRepository cursoRepository) {
        this.horarioRepository = horarioRepository;
        this.jornadaRepository = jornadaRepository;
        this.cicloRepository = cicloRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    public Horario buscarPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado (id " + id + ")."));
    }

    @Override
    public Jornada buscarJornada(Long cicloId, DiaSemana dia, Turno turno) {
        if (cicloId == null || dia == null || turno == null) return null;
        return jornadaRepository.findByCicloIdAndDiaSemanaAndTurno(cicloId, dia, turno).orElse(null);
    }

    @Override
    public Map<DiaSemana, Map<Turno, Jornada>> agruparParaGrilla(Long cicloId) {
        Map<DiaSemana, Map<Turno, Jornada>> mapa = new LinkedHashMap<>();
        for (DiaSemana d : DiaSemana.values()) {
            Map<Turno, Jornada> porTurno = new LinkedHashMap<>();
            for (Turno t : Turno.values()) {
                porTurno.put(t, null);
            }
            mapa.put(d, porTurno);
        }
        if (cicloId == null) return mapa;

        List<Jornada> jornadas = jornadaRepository.findParaGrilla(cicloId);
        for (Jornada j : jornadas) {
            mapa.get(j.getDiaSemana()).put(j.getTurno(), j);
        }
        return mapa;
    }

    @Override
    @Transactional
    public void guardarJornada(Long cicloId, DiaSemana dia, Turno turno, LocalTime horaInicio, LocalTime horaFin,
                               List<Long> cursoIds) {
        Jornada jornada = jornadaRepository.findByCicloIdAndDiaSemanaAndTurno(cicloId, dia, turno).orElse(null);

        if (jornada == null) {
            if (horaInicio == null || horaFin == null) {
                throw new IllegalArgumentException("Debes indicar la hora de inicio y fin de la jornada.");
            }
            if (!horaFin.isAfter(horaInicio)) {
                throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
            }
            Ciclo ciclo = cicloRepository.findById(cicloId)
                    .orElseThrow(() -> new IllegalArgumentException("Ciclo no encontrado: " + cicloId));
            jornada = new Jornada();
            jornada.setCiclo(ciclo);
            jornada.setDiaSemana(dia);
            jornada.setTurno(turno);
            jornada.setHoraInicio(horaInicio);
            jornada.setHoraFin(horaFin);
            jornadaRepository.save(jornada);
        }

        if (cursoIds == null || cursoIds.isEmpty()) {
            if (jornada.getHorarios().isEmpty()) {
                throw new IllegalArgumentException("Selecciona al menos un curso para la jornada.");
            }
            return;
        }

        for (Long cursoId : cursoIds) {
            if (horarioRepository.existsByJornadaIdAndCursoId(jornada.getId(), cursoId)) {
                continue;
            }
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));
            Horario horario = new Horario();
            horario.setCurso(curso);
            jornada.addHorario(horario);
        }
        jornadaRepository.save(jornada);
    }

    @Override
    @Transactional
    public void editarHoras(Long jornadaId, LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null || !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }
        Jornada jornada = jornadaRepository.findById(jornadaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Jornada no encontrada (id " + jornadaId + ")."));
        jornada.setHoraInicio(horaInicio);
        jornada.setHoraFin(horaFin);
        jornadaRepository.save(jornada);
    }

    @Override
    @Transactional
    public void quitarCurso(Long horarioId) {
        horarioRepository.deleteById(horarioId);
    }

    @Override
    @Transactional
    public void eliminarJornada(Long jornadaId) {
        jornadaRepository.deleteById(jornadaId);
    }

    @Override
    public List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia) {
        if (cicloId == null || turno == null || dia == null) {
            return List.of();
        }
        return horarioRepository.findByCicloIdAndTurnoAndDiaSemana(cicloId, turno, dia);
    }
}
