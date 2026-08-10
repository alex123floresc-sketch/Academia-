package com.unaj.project.service;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Rol;
import com.unaj.project.model.SolicitudEliminacionUsuario;
import com.unaj.project.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UsuarioService {

    /** Resultado de intentar eliminar un usuario: los administradores no se borran de inmediato, ver eliminar(). */
    enum ResultadoEliminacion { ELIMINADO, SOLICITUD_CREADA, APROBACION_REGISTRADA, YA_APROBADO }

    List<Usuario> listarTodos();
    Page<Usuario> buscarPagina(String q, Pageable pageable);
    Usuario buscarPorId(Long id);
    UsuarioForm buscarFormPorId(Long id);
    List<Rol> listarRoles();
    void guardar(UsuarioForm form);

    /**
     * Elimina un usuario. Si el objetivo tiene rol Administrador, no se borra al toque: cada
     * llamada de un administrador distinto cuenta como una aprobación, y recién se borra cuando
     * se junta el número de aprobaciones definido por aprobacionesRequeridas().
     */
    ResultadoEliminacion eliminar(Long id, String solicitanteUsername);

    void cancelarSolicitudEliminacion(Long usuarioObjetivoId);

    /** Solicitudes de eliminación pendientes, indexadas por el id del usuario objetivo. */
    Map<Long, SolicitudEliminacionUsuario> solicitudesPendientesPorObjetivo();

    int aprobacionesRequeridas();
}
