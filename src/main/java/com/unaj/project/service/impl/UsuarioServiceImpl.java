package com.unaj.project.service.impl;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Rol;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RolRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    /** Cuenta sembrada por DataInitializer: acceso total, protegida contra borrado y contra
     *  que se le quite el rol de administrador o se la desactive. */
    private static final String USERNAME_DESARROLLADOR = "desarrollador";
    private static final String ROL_ADMIN = "ROLE_ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder encoder;
    private final RegistroActividadService registroActividadService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              BCryptPasswordEncoder encoder,
                              RegistroActividadService registroActividadService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.encoder = encoder;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Usuario> listarTodos() { return usuarioRepository.findAll(); }

    @Override
    public Page<Usuario> buscarPagina(String q, String solicitanteUsername, Pageable pageable) {
        boolean ocultarDesarrollador = !USERNAME_DESARROLLADOR.equals(solicitanteUsername);
        return usuarioRepository.buscar(q, ocultarDesarrollador, pageable);
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado (id " + id + ")."));
    }

    @Override
    public Usuario buscarVisiblePorId(Long id, String solicitanteUsername) {
        Usuario usuario = buscarPorId(id);
        verificarVisiblePara(usuario, solicitanteUsername);
        return usuario;
    }

    @Override
    public UsuarioForm buscarFormPorId(Long id, String solicitanteUsername) {
        Usuario usuario = buscarVisiblePorId(id, solicitanteUsername);
        boolean objetivoEsAdmin = usuario.getRoles() != null
                && usuario.getRoles().stream().anyMatch(r -> ROL_ADMIN.equals(r.getNombre()));
        if (objetivoEsAdmin && !USERNAME_DESARROLLADOR.equals(solicitanteUsername)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado (id " + id + ").");
        }
        return aForm(usuario);
    }

    @Override
    public List<Rol> listarRoles() { return rolRepository.findAll(); }

    @Override
    public List<Rol> listarRolesAsignablesPor(String solicitanteUsername) {
        List<Rol> roles = rolRepository.findAll();
        if (USERNAME_DESARROLLADOR.equals(solicitanteUsername)) {
            return roles;
        }
        return roles.stream().filter(r -> !ROL_ADMIN.equals(r.getNombre())).toList();
    }

    @Override
    @Transactional
    public void guardar(UsuarioForm form, String solicitanteUsername) {
        Usuario usuario = (form.getId() != null) ? buscarPorId(form.getId()) : new Usuario();
        boolean esDesarrolladorObjetivo = USERNAME_DESARROLLADOR.equals(usuario.getUsername());
        boolean esDesarrolladorSolicitante = USERNAME_DESARROLLADOR.equals(solicitanteUsername);

        if (esDesarrolladorObjetivo) {
            verificarVisiblePara(usuario, solicitanteUsername);
            if (!USERNAME_DESARROLLADOR.equals(form.getUsername())) {
                throw new IllegalArgumentException("La cuenta de desarrollador no se puede renombrar.");
            }
            if (!form.isActivo()) {
                throw new IllegalArgumentException("La cuenta de desarrollador no se puede desactivar.");
            }
        } else if (form.getId() == null && USERNAME_DESARROLLADOR.equalsIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Ese nombre de usuario está reservado.");
        }

        Rol rol = rolRepository.findById(form.getRolId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + form.getRolId()));
        if (esDesarrolladorObjetivo && !ROL_ADMIN.equals(rol.getNombre())) {
            throw new IllegalArgumentException("La cuenta de desarrollador siempre debe tener rol Administrador.");
        }

        boolean objetivoEraAdmin = !esDesarrolladorObjetivo && usuario.getRoles() != null
                && usuario.getRoles().stream().anyMatch(r -> ROL_ADMIN.equals(r.getNombre()));
        boolean asignaAdmin = ROL_ADMIN.equals(rol.getNombre());
        if (!esDesarrolladorObjetivo && !esDesarrolladorSolicitante && (objetivoEraAdmin || asignaAdmin)) {
            throw new IllegalArgumentException("Solo la cuenta de desarrollador puede crear, editar o eliminar administradores.");
        }

        usuario.setUsername(form.getUsername());
        usuario.setNombre(form.getNombre());
        usuario.setActivo(form.isActivo());
        usuario.setRoles(Set.of(rol));

        if (form.getPasswordPlano() != null && !form.getPasswordPlano().isBlank()) {
            usuario.setPassword(encoder.encode(form.getPasswordPlano()));
        }

        boolean esNuevo = (usuario.getId() == null);
        usuarioRepository.save(usuario);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Usuarios",
                usuario.getId(), (esNuevo ? "Creó al usuario " : "Editó al usuario ") + usuario.getUsername());
    }

    @Override
    @Transactional
    public void eliminar(Long id, String solicitanteUsername) {
        Usuario objetivo = buscarPorId(id);
        verificarVisiblePara(objetivo, solicitanteUsername);
        if (USERNAME_DESARROLLADOR.equals(objetivo.getUsername())) {
            throw new IllegalArgumentException("La cuenta de desarrollador no se puede eliminar.");
        }

        boolean objetivoEsAdmin = objetivo.getRoles() != null
                && objetivo.getRoles().stream().anyMatch(r -> ROL_ADMIN.equals(r.getNombre()));
        if (objetivoEsAdmin && !USERNAME_DESARROLLADOR.equals(solicitanteUsername)) {
            throw new IllegalArgumentException("Solo la cuenta de desarrollador puede eliminar administradores.");
        }

        usuarioRepository.delete(objetivo);
        registroActividadService.registrar(solicitanteUsername, TipoAccion.ELIMINAR, "Usuarios",
                id, "Eliminó al usuario " + objetivo.getUsername());
    }

    /** La cuenta 'desarrollador' no existe para nadie más que ella misma: cualquier otro admin
     *  que intente verla, editarla o borrarla (por URL directa u otro medio) recibe el mismo
     *  error que si el id no existiera, en vez de una respuesta que confirme que la cuenta existe. */
    private void verificarVisiblePara(Usuario objetivo, String solicitanteUsername) {
        if (USERNAME_DESARROLLADOR.equals(objetivo.getUsername()) && !USERNAME_DESARROLLADOR.equals(solicitanteUsername)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado (id " + objetivo.getId() + ").");
        }
    }

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
