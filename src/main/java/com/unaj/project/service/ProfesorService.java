package com.unaj.project.service;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.model.Profesor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProfesorService {
    List<Profesor> listarTodos();
    Page<Profesor> buscarPagina(String q, Pageable pageable);
    Profesor buscarPorId(Long id);
    ProfesorForm buscarFormPorId(Long id);
    void guardar(ProfesorForm form);
    void eliminar(Long id);
}