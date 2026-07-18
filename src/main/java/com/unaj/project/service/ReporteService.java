package com.unaj.project.service;

import com.unaj.project.dto.AlumnoMorosoDTO;
import com.unaj.project.dto.AlumnosPorCicloTurnoDTO;
import com.unaj.project.dto.IngresoMensualDTO;

import java.util.List;

public interface ReporteService {
    List<AlumnosPorCicloTurnoDTO> alumnosPorCicloTurno();
    List<IngresoMensualDTO> ingresosPorMes();
    List<AlumnoMorosoDTO> alumnosMorosos();
}
