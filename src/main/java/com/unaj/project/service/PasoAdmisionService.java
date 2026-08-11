package com.unaj.project.service;

import com.unaj.project.dto.PasoAdmisionForm;
import com.unaj.project.model.PasoAdmision;

import java.util.List;

public interface PasoAdmisionService {

    List<PasoAdmision> listarTodos();

    List<PasoAdmision> listarActivos();

    PasoAdmision buscarPorId(Long id);

    PasoAdmisionForm buscarFormPorId(Long id);

    void guardar(PasoAdmisionForm form);

    void eliminar(Long id);
}
