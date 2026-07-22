package com.unaj.project.service;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AlumnoService {
    List<Alumno> listarTodos();
    Page<Alumno> buscarPagina(String q, Pageable pageable);
    Alumno buscarPorId(Long id);
    AlumnoForm buscarFormPorId(Long id);
    void guardar(AlumnoForm form);
    void eliminar(Long id);
}