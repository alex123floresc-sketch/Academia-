package com.unaj.project.service;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.model.Ciclo;
import java.util.List;

public interface CicloService {
    List<Ciclo> listarTodos();
    Ciclo buscarPorId(Long id);
    CicloForm buscarFormPorId(Long id);
    Ciclo obtenerActivo();
    void guardar(CicloForm form);
    void eliminar(Long id);
}