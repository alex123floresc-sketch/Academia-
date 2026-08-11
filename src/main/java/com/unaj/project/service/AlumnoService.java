package com.unaj.project.service;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Nivel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface AlumnoService {
    List<Alumno> listarTodos();
    Page<Alumno> buscarPagina(String q, Pageable pageable);
    Page<Alumno> buscarPagina(String q, Nivel nivel, String area, Pageable pageable);
    Page<Alumno> buscarPagina(String q, Nivel nivel, String area, Long salonId, Pageable pageable);
    Alumno buscarPorId(Long id);
    AlumnoForm buscarFormPorId(Long id);
    Alumno guardar(AlumnoForm form);
    void eliminar(Long id);

    /** Cantidad de alumnos de un nivel, por cada clasificación de {@link com.unaj.project.model.Areas#paraNivel(Nivel)} (área o grado, según el nivel). Las clasificaciones del nivel siempre aparecen, en ese orden, seguidas de "Sin área" si aplica. */
    Map<String, Long> contarPorArea(Nivel nivel);

    /** Cantidad de alumnos por nivel. */
    Map<Nivel, Long> contarPorNivel();
}
