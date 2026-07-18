package com.unaj.project.service;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import java.util.List;

public interface AlumnoService {
    List<Alumno> listarTodos();
    Alumno buscarPorId(Long id);
    AlumnoForm buscarFormPorId(Long id);   // para rellenar el formulario de edición
    void guardar(AlumnoForm form);
    void eliminar(Long id);
}