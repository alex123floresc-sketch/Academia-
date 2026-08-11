package com.unaj.project.service;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Rol;
import com.unaj.project.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsuarioService {

    List<Usuario> listarTodos();

    /**
     * Lista paginada de usuarios. La cuenta 'desarrollador' se excluye por completo salvo que
     * quien pregunta sea ella misma: no debe aparecer, ni en la búsqueda, para ningún otro admin.
     */
    Page<Usuario> buscarPagina(String q, String solicitanteUsername, Pageable pageable);

    Usuario buscarPorId(Long id);

    /** Igual que buscarPorId, pero lanza RecursoNoEncontradoException si el id es el de
     *  'desarrollador' y el solicitante no lo es (para endpoints como la foto de perfil). */
    Usuario buscarVisiblePorId(Long id, String solicitanteUsername);

    /** Lanza RecursoNoEncontradoException si el id es el de 'desarrollador' y el solicitante no lo es. */
    UsuarioForm buscarFormPorId(Long id, String solicitanteUsername);

    /** Todos los roles, incluido Administrador (para la cuenta 'desarrollador'). */
    List<Rol> listarRoles();

    /** Roles que puede asignar solicitanteUsername: sin Administrador, salvo que sea 'desarrollador'. */
    List<Rol> listarRolesAsignablesPor(String solicitanteUsername);

    /** Lanza RecursoNoEncontradoException si se intenta editar 'desarrollador' desde otra cuenta. */
    void guardar(UsuarioForm form, String solicitanteUsername);

    /**
     * Elimina un usuario. Si el objetivo tiene rol Administrador, solo la cuenta 'desarrollador'
     * puede eliminarlo (o crearlo, o editarlo) — ver guardar(). Cualquier otro intento lanza
     * IllegalArgumentException.
     */
    void eliminar(Long id, String solicitanteUsername);
}
