package com.unaj.project.service;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Rol;
import com.unaj.project.model.Usuario;
import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Usuario buscarPorId(Long id);
    UsuarioForm buscarFormPorId(Long id);
    List<Rol> listarRoles();
    void guardar(UsuarioForm form);
    void eliminar(Long id);
}