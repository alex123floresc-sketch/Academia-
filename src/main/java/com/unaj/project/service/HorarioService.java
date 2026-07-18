// src/main/java/com/unaj/project/service/HorarioService.java
package com.unaj.project.service;

import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Turno;

import java.util.List;
import java.util.Map;

public interface HorarioService {
    Horario buscarPorId(Long id);
    void guardar(Horario horario);   // valida cruces de hora
    void eliminar(Long id);

    /** Horarios agrupados por día, listos para pintar la grilla. */
    Map<DiaSemana, List<Horario>> agruparPorDia(Long cicloId, Turno turno);
}