package com.unaj.project.service;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.model.Asistencia;

import java.util.List;

public interface AsistenciaService {

    List<Asistencia> listarDeHoy(Long horarioId);

    long contarDeHoy(Long horarioId);

    AsistenciaResultadoDTO registrar(Long horarioId, String codigoQr, String username);
}
