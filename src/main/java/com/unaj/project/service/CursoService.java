package com.unaj.project.service;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Curso;
import java.util.List;

public interface CursoService {
    List<Curso> listarTodos();
    Curso buscarPorId(Long id);
    CursoForm buscarFormPorId(Long id);
    void guardar(CursoForm form);
    void eliminar(Long id);
}