package com.unaj.project.service.impl;

import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Turno;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.HorarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioServiceImpl(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @Override
    public Horario buscarPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado (id " + id + ")."));
    }

    @Override
    @Transactional
    public void guardar(Horario horario) {
        if (horario.getHoraFin().isBefore(horario.getHoraInicio())
                || horario.getHoraFin().equals(horario.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }

        List<Horario> mismosDia = horarioRepository.findByCicloIdAndTurnoAndDiaSemana(
                horario.getCiclo().getId(), horario.getTurno(), horario.getDiaSemana());

        for (Horario h : mismosDia) {
            if (h.getId().equals(horario.getId())) continue;
            boolean seSolapan = horario.getHoraInicio().isBefore(h.getHoraFin())
                    && h.getHoraInicio().isBefore(horario.getHoraFin());
            if (seSolapan) {
                throw new IllegalStateException(
                        "Cruce de horario con " + h.getCurso().getNombre()
                                + " (" + h.getHoraInicio() + " - " + h.getHoraFin() + ").");
            }
        }

        horarioRepository.save(horario);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        horarioRepository.deleteById(id);
    }

    @Override
    public Map<DiaSemana, List<Horario>> agruparPorDia(Long cicloId, Turno turno) {
        List<Horario> horarios = horarioRepository.findParaGrilla(cicloId, turno);
        Map<DiaSemana, List<Horario>> mapa = new LinkedHashMap<>();
        for (DiaSemana d : DiaSemana.values()) {
            mapa.put(d, new ArrayList<>());
        }
        for (Horario h : horarios) {
            mapa.get(h.getDiaSemana()).add(h);
        }
        return mapa;
    }
}