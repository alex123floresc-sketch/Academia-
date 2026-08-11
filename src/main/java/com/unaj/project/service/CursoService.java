package com.unaj.project.service;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Nivel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface CursoService {
    List<Curso> listarTodos();
    List<Curso> listarDestacados();
    List<Curso> listarPorNivel(Nivel nivel);
    List<Curso> listarPorNivelYArea(Nivel nivel, String area);
    Page<Curso> buscarPagina(String q, Pageable pageable);
    Page<Curso> buscarPagina(String q, Nivel nivel, String area, Pageable pageable);
    Curso buscarPorId(Long id);
    CursoForm buscarFormPorId(Long id);
    void guardar(CursoForm form);
    void eliminar(Long id);
    void establecerCursosDeArea(Nivel nivel, String area, List<Long> cursoIds);

    /** Cantidad de cursos (no eliminados) de un nivel, por cada clasificación de {@link com.unaj.project.model.Areas#paraNivel(Nivel)}, en ese orden. Un curso con varias clasificaciones cuenta una vez por cada una. */
    Map<String, Long> contarPorArea(Nivel nivel);

    /** Igual que {@link #contarPorArea(Nivel)} pero con la lista de cursos en vez del conteo; solo incluye clasificaciones con al menos un curso. */
    Map<String, List<Curso>> mallaPorNivel(Nivel nivel);

    /** Cantidad de cursos (no eliminados) por nivel. */
    Map<Nivel, Long> contarPorNivel();
}
