package com.unaj.project.service;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CursoService {
    List<Curso> listarTodos();
    Page<Curso> buscarPagina(String q, Pageable pageable);
    Curso buscarPorId(Long id);
    CursoForm buscarFormPorId(Long id);
    void guardar(CursoForm form);
    void eliminar(Long id);
}