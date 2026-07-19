package com.unaj.project.service.impl;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Rol;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RolRepository;
import com.unaj.project.repository.UsuarioRepository;
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

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              BCryptPasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
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

        usuario.setUsername(form.getUsername());
        usuario.setNombre(form.getNombre());
        usuario.setActivo(form.isActivo());

        Rol rol = rolRepository.findById(form.getRolId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + form.getRolId()));
        usuario.setRoles(Set.of(rol));

        // Solo cifrar si escribieron una contraseña nueva
        if (form.getPasswordPlano() != null && !form.getPasswordPlano().isBlank()) {
            usuario.setPassword(encoder.encode(form.getPasswordPlano()));
        }
        // Si viene vacía en edición, conserva la actual (ya está en el objeto cargado).
        // Si viene vacía en creación, el controller ya lo habrá bloqueado con validación.

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Long id) { usuarioRepository.deleteById(id); }

    private UsuarioForm aForm(Usuario u) {
        UsuarioForm form = new UsuarioForm();
        form.setId(u.getId());
        form.setUsername(u.getUsername());
        form.setNombre(u.getNombre());
        form.setActivo(u.isActivo());
        // El rol: tomamos el primero (tu modelo usa un rol por usuario en la práctica)
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            form.setRolId(u.getRoles().iterator().next().getId());
        }
        // passwordPlano se deja vacío a propósito (no se muestra la contraseña actual)
        return form;
    }
}