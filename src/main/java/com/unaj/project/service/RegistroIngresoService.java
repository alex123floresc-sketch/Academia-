package com.unaj.project.service;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.model.RegistroIngreso;

import java.util.List;

public interface RegistroIngresoService {

    List<RegistroIngreso> listarDeHoy();

    long contarDeHoy();

    AsistenciaResultadoDTO registrar(String codigo, String username);
}
