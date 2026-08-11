package com.unaj.project.service;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.model.Ciclo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CicloService {
    List<Ciclo> listarTodos();
    Page<Ciclo> buscarPagina(String q, Pageable pageable);
    Ciclo buscarPorId(Long id);
    CicloForm buscarFormPorId(Long id);
    Ciclo obtenerActivo();
    void guardar(CicloForm form);
    void eliminar(Long id);
}