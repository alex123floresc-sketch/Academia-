package com.unaj.project.service;

import com.unaj.project.dto.PerfilForm;
import com.unaj.project.model.Usuario;

public interface PerfilService {
    Usuario obtenerPropio(String username);
    Usuario obtenerPorId(Long id);
    PerfilForm formPropio(String username);
    void actualizarPropio(String username, PerfilForm form);
}
