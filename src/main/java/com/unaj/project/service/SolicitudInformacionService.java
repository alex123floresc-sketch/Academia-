package com.unaj.project.service;

import com.unaj.project.dto.SolicitudInformacionForm;
import com.unaj.project.model.SolicitudInformacion;

import java.util.List;

public interface SolicitudInformacionService {

    void registrar(SolicitudInformacionForm form);

    List<SolicitudInformacion> listarTodas();

    long contarNoAtendidas();

    void marcarAtendida(Long id, boolean atendida);
}
