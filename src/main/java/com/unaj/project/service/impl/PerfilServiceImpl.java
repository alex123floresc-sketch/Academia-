package com.unaj.project.service.impl;

import com.unaj.project.dto.PerfilForm;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.PerfilService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder;
    private final RegistroActividadService registroActividadService;

    public PerfilServiceImpl(UsuarioRepository usuarioRepository, BCryptPasswordEncoder encoder,
                             RegistroActividadService registroActividadService) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public Usuario obtenerPropio(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            throw new IllegalStateException("No se pudo identificar la cuenta actual.");
        }
        return usuario;
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public PerfilForm formPropio(String username) {
        Usuario usuario = obtenerPropio(username);
        PerfilForm form = new PerfilForm();
        form.setNombre(usuario.getNombre());
        form.setEmail(usuario.getEmail());
        form.setTelefono(usuario.getTelefono());
        form.setCargo(usuario.getCargo());
        return form;
    }

    @Override
    @Transactional
    public void actualizarPropio(String username, PerfilForm form) {
        Usuario usuario = obtenerPropio(username);
        usuario.setNombre(form.getNombre());
        usuario.setEmail(blankToNull(form.getEmail()));
        usuario.setTelefono(blankToNull(form.getTelefono()));
        usuario.setCargo(blankToNull(form.getCargo()));

        boolean cambioFoto = false;
        if (form.isQuitarFoto()) {
            usuario.setFoto(null);
            usuario.setFotoContentType(null);
            cambioFoto = true;
        }
        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                usuario.setFoto(foto.getBytes());
                usuario.setFotoContentType(foto.getContentType());
                cambioFoto = true;
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        if (form.isQuitarFirma()) {
            usuario.setFirma(null);
            usuario.setFirmaContentType(null);
        }
        MultipartFile firma = form.getFirma();
        if (firma != null && !firma.isEmpty()) {
            try {
                usuario.setFirma(firma.getBytes());
                usuario.setFirmaContentType(firma.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la firma enviada.", e);
            }
        }

        boolean cambioPassword = false;
        String passwordNueva = form.getPasswordNueva();
        if (passwordNueva != null && !passwordNueva.isBlank()) {
            if (passwordNueva.length() < 6) {
                throw new IllegalArgumentException("La contraseña nueva debe tener al menos 6 caracteres.");
            }
            if (!passwordNueva.equals(form.getPasswordNuevaConfirmar())) {
                throw new IllegalArgumentException("La confirmación no coincide con la contraseña nueva.");
            }
            String passwordActual = form.getPasswordActual();
            if (passwordActual == null || passwordActual.isBlank()
                    || !encoder.matches(passwordActual, usuario.getPassword())) {
                throw new IllegalArgumentException("La contraseña actual no es correcta.");
            }
            usuario.setPassword(encoder.encode(passwordNueva));
            cambioPassword = true;
        }

        usuarioRepository.save(usuario);

        String descripcion = "Actualizó su perfil";
        if (cambioFoto && cambioPassword) {
            descripcion = "Actualizó su perfil, cambió su foto y su contraseña";
        } else if (cambioFoto) {
            descripcion = "Actualizó su perfil y cambió su foto";
        } else if (cambioPassword) {
            descripcion = "Actualizó su perfil y cambió su contraseña";
        }
        registroActividadService.registrar(username, TipoAccion.EDITAR, "Usuarios", usuario.getId(), descripcion);
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
