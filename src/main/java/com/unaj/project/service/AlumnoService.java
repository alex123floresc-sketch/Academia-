package com.unaj.project.service;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface AlumnoService {
    List<Alumno> listarTodos();
    Page<Alumno> buscarPagina(String q, Pageable pageable);
    Page<Alumno> buscarPagina(String q, String area, Pageable pageable);
    Alumno buscarPorId(Long id);
    AlumnoForm buscarFormPorId(Long id);
    Alumno guardar(AlumnoForm form);
    void eliminar(Long id);

    /** Cantidad de alumnos por área (una sola pasada, comparación insensible a mayúsculas contra {@link com.unaj.project.model.Areas#TODAS}). Las áreas de {@code Areas.TODAS} siempre aparecen, en ese orden, seguidas de "Sin área" si aplica. */
    Map<String, Long> contarPorArea();
}