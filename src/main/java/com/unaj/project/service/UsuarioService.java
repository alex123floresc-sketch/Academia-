package com.unaj.project.service;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Rol;
import com.unaj.project.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Page<Usuario> buscarPagina(String q, Pageable pageable);
    Usuario buscarPorId(Long id);
    UsuarioForm buscarFormPorId(Long id);
    List<Rol> listarRoles();
    void guardar(UsuarioForm form);
    void eliminar(Long id);
}