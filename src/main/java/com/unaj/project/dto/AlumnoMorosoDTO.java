package com.unaj.project.dto;

import java.math.BigDecimal;

public record AlumnoMorosoDTO(Long alumnoId, String nombre, String apellido, String email,
                              long pagosVencidos, BigDecimal montoAdeudado) {
}
