package com.unaj.project.service;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.model.Asistencia;

import java.util.List;

public interface AsistenciaService {

    /** Asistencias ya registradas hoy para un horario, con el alumno cargado. */
    List<Asistencia> listarDeHoy(Long horarioId);

    long contarDeHoy(Long horarioId);

    /**
     * Registra la asistencia de un alumno (identificado por el código QR escaneado) a un horario.
     * No lanza excepciones de negocio: los errores se comunican en el resultado (ok=false).
     */
    AsistenciaResultadoDTO registrar(Long horarioId, String codigoQr, String username);
}
