package com.unaj.project.service;

import com.unaj.project.dto.PreguntaFrecuenteForm;
import com.unaj.project.model.PreguntaFrecuente;

import java.util.List;

public interface PreguntaFrecuenteService {

    List<PreguntaFrecuente> listarTodas();

    List<PreguntaFrecuente> listarActivas();

    PreguntaFrecuente buscarPorId(Long id);

    PreguntaFrecuenteForm buscarFormPorId(Long id);

    void guardar(PreguntaFrecuenteForm form);

    void eliminar(Long id);
}
