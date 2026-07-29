package com.unaj.project.dto;

import com.unaj.project.model.BloqueHorario;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;

import java.util.List;
import java.util.Map;

public record FilaHorarioDTO(BloqueHorario bloque, Map<DiaSemana, List<Horario>> porDia) {

    public List<Horario> cursosDe(DiaSemana dia) {
        return porDia.getOrDefault(dia, List.of());
    }
}
