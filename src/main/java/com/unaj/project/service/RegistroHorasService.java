package com.unaj.project.service;

import com.unaj.project.model.RegistroHoras;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface RegistroHorasService {

    RegistroHoras marcarLlegada(Long horarioId, LocalDate fecha, String username);

    RegistroHoras registrarHoras(Long horarioId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                 String observaciones, String username);

    List<RegistroHoras> listarPorProfesor(Long profesorId);

    List<RegistroHoras> listarPorProfesorEnRango(Long profesorId, LocalDate desde, LocalDate hasta);

    Map<Long, RegistroHoras> buscarDeHoyPorHorarios(List<Long> horarioIds);
}
