package com.unaj.project.service.impl;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Rol;
import com.unaj.project.model.SolicitudEliminacionUsuario;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RolRepository;
import com.unaj.project.repository.SolicitudEliminacionUsuarioRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    /** Cuenta sembrada por DataInitializer: acceso total, protegida contra borrado y contra
     *  que se le quite el rol de administrador o se la desactive. */
    private static final String USERNAME_DESARROLLADOR = "desarrollador";
    private static final String ROL_ADMIN = "ROLE_ADMIN";

    /** Nadie borra a un administrador por su cuenta: hacen falta este número de administradores
     *  distintos de acuerdo (el que solicita cuenta como el primero). */
    private static final int APROBACIONES_REQUERIDAS = 3;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SolicitudEliminacionUsuarioRepository solicitudRepository;
    private final BCryptPasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              SolicitudEliminacionUsuarioRepository solicitudRepository,
                              BCryptPasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.solicitudRepository = solicitudRepository;
        this.encoder = encoder;
    }

    @Override
    public List<Usuario> listarTodos() { return usuarioRepository.findAll(); }

    @Override
    public Page<Usuario> buscarPagina(String q, Pageable pageable) {
        return usuarioRepository.buscar(q, pageable);
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado (id " + id + ")."));
    }

    @Override
    public UsuarioForm buscarFormPorId(Long id) {
        return aForm(buscarPorId(id));
    }

    @Override
    public List<Rol> listarRoles() { return rolRepository.findAll(); }

    @Override
    @Transactional
    public void guardar(UsuarioForm form) {
        Usuario usuario = (form.getId() != null) ? buscarPorId(form.getId()) : new Usuario();
        boolean esDesarrollador = USERNAME_DESARROLLADOR.equals(usuario.getUsername());

        if (esDesarrollador) {
            if (!USERNAME_DESARROLLADOR.equals(form.getUsername())) {
                throw new IllegalArgumentException("La cuenta de desarrollador no se puede renombrar.");
            }
            if (!form.isActivo()) {
                throw new IllegalArgumentException("La cuenta de desarrollador no se puede desactivar.");
            }
        }

        Rol rol = rolRepository.findById(form.getRolId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + form.getRolId()));
        if (esDesarrollador && !ROL_ADMIN.equals(rol.getNombre())) {
            throw new IllegalArgumentException("La cuenta de desarrollador siempre debe tener rol Administrador.");
        }

        usuario.setUsername(form.getUsername());
        usuario.setNombre(form.getNombre());
        usuario.setActivo(form.isActivo());
        usuario.setRoles(Set.of(rol));

        if (form.getPasswordPlano() != null && !form.getPasswordPlano().isBlank()) {
            usuario.setPassword(encoder.encode(form.getPasswordPlano()));
        }

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public ResultadoEliminacion eliminar(Long id, String solicitanteUsername) {
        Usuario objetivo = buscarPorId(id);
        if (USERNAME_DESARROLLADOR.equals(objetivo.getUsername())) {
            throw new IllegalArgumentException("La cuenta de desarrollador no se puede eliminar.");
        }

        boolean objetivoEsAdmin = objetivo.getRoles() != null
                && objetivo.getRoles().stream().anyMatch(r -> ROL_ADMIN.equals(r.getNombre()));
        if (!objetivoEsAdmin) {
            usuarioRepository.delete(objetivo);
            return ResultadoEliminacion.ELIMINADO;
        }

        Usuario solicitante = usuarioRepository.findByUsername(solicitanteUsername);
        if (solicitante == null) {
            throw new IllegalStateException("No se pudo identificar al usuario que solicita la eliminación.");
        }

        SolicitudEliminacionUsuario solicitud = solicitudRepository.findByObjetivoIdParaActualizar(id)
                .orElse(null);
        boolean esNuevaSolicitud = (solicitud == null);
        if (esNuevaSolicitud) {
            solicitud = new SolicitudEliminacionUsuario();
            solicitud.setObjetivo(objetivo);
            solicitud.setSolicitadoPor(solicitante);
            solicitud.setCreadoEn(LocalDateTime.now());
        }

        boolean yaAprobo = solicitud.getAprobadoPor().stream()
                .anyMatch(u -> u.getId().equals(solicitante.getId()));
        if (yaAprobo) {
            return ResultadoEliminacion.YA_APROBADO;
        }
        solicitud.getAprobadoPor().add(solicitante);

        if (solicitud.getAprobadoPor().size() >= APROBACIONES_REQUERIDAS) {
            if (!esNuevaSolicitud) {
                solicitudRepository.delete(solicitud);
                solicitudRepository.flush();
            }
            usuarioRepository.delete(objetivo);
            return ResultadoEliminacion.ELIMINADO;
        }

        solicitudRepository.save(solicitud);
        return esNuevaSolicitud ? ResultadoEliminacion.SOLICITUD_CREADA : ResultadoEliminacion.APROBACION_REGISTRADA;
    }

    @Override
    @Transactional
    public void cancelarSolicitudEliminacion(Long usuarioObjetivoId) {
        solicitudRepository.deleteByObjetivoId(usuarioObjetivoId);
    }

    @Override
    public Map<Long, SolicitudEliminacionUsuario> solicitudesPendientesPorObjetivo() {
        return solicitudRepository.findAllConDetalle().stream()
                .collect(Collectors.toMap(s -> s.getObjetivo().getId(), s -> s));
    }

    @Override
    public int aprobacionesRequeridas() { return APROBACIONES_REQUERIDAS; }

    private UsuarioForm aForm(Usuario u) {
        UsuarioForm form = new UsuarioForm();
        form.setId(u.getId());
        form.setUsername(u.getUsername());
        form.setNombre(u.getNombre());
        form.setActivo(u.isActivo());
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            form.setRolId(u.getRoles().iterator().next().getId());
        }
        return form;
    }
}
