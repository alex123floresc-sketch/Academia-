package com.unaj.project.dto;

import java.time.LocalDate;

/** estado es null para celdas de relleno (días fuera del rango del ciclo, solo para alinear la grilla). */
public record DiaCalendarioAsistenciaDTO(LocalDate fecha, EstadoAsistenciaDia estado) {
}
