package com.unaj.project.dto;

import java.time.LocalDate;
import java.util.List;

public record AsistenciaResumenAlumnoDTO(
        Long cicloId,
        String cicloNombre,
        LocalDate desde,
        LocalDate hasta,
        int totalSesiones,
        int totalAsistencias,
        int totalFaltas,
        int totalTardanzas,
        List<CursoAsistenciaResumenDTO> porCurso,
        List<MesCalendarioAsistenciaDTO> meses
) {

    public static AsistenciaResumenAlumnoDTO vacio(Long cicloId, String cicloNombre) {
        return new AsistenciaResumenAlumnoDTO(cicloId, cicloNombre, null, null,
                0, 0, 0, 0, List.of(), List.of());
    }

    public double porcentajeAsistencia() {
        return totalSesiones == 0 ? 0.0 : (totalAsistencias * 100.0) / totalSesiones;
    }
}
