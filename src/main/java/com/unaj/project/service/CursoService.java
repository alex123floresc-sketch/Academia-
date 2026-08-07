package com.unaj.project.service;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface CursoService {
    List<Curso> listarTodos();
    List<Curso> listarPorArea(String area);
    Page<Curso> buscarPagina(String q, Pageable pageable);
    Page<Curso> buscarPagina(String q, String area, Pageable pageable);
    Curso buscarPorId(Long id);
    CursoForm buscarFormPorId(Long id);
    void guardar(CursoForm form);
    void eliminar(Long id);
    void establecerCursosDeArea(String area, List<Long> cursoIds);

    /** Cantidad de cursos (no eliminados) por área de {@link com.unaj.project.model.Areas#TODAS}, en ese orden. Un curso con varias áreas cuenta una vez por cada una. */
    Map<String, Long> contarPorArea();
}