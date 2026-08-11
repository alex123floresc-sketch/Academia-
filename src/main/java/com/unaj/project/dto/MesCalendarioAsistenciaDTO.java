package com.unaj.project.dto;

import java.time.YearMonth;
import java.util.List;

public record MesCalendarioAsistenciaDTO(YearMonth mes, String etiqueta, List<List<DiaCalendarioAsistenciaDTO>> semanas) {
}
