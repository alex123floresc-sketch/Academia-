package com.unaj.project.service;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.model.Profesor;
import java.util.List;

public interface ProfesorService {
    List<Profesor> listarTodos();
    Profesor buscarPorId(Long id);
    ProfesorForm buscarFormPorId(Long id);
    void guardar(ProfesorForm form);
    void eliminar(Long id);
}