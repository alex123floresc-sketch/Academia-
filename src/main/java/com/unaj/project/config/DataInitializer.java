// src/main/java/com/unaj/project/config/DataInitializer.java
package com.unaj.project.config;

import com.unaj.project.model.Rol;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RolRepository;
import com.unaj.project.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           BCryptPasswordEncoder encoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Rol admin  = obtenerOCrearRol("ROLE_ADMIN");
        Rol cajero = obtenerOCrearRol("ROLE_CAJERO");

        if (usuarioRepository.findByUsername("admin") == null) {
            crearUsuario("admin", "admin123", "Administrador", Set.of(admin));
        }
        if (usuarioRepository.findByUsername("cajero") == null) {
            crearUsuario("cajero", "cajero123", "Cajero", Set.of(cajero));
        }
    }

    private Rol obtenerOCrearRol(String nombre) {
        Rol rol = rolRepository.findByNombre(nombre);
        if (rol == null) {
            rol = new Rol();
            rol.setNombre(nombre);
            rol = rolRepository.save(rol);
        }
        return rol;
    }

    private void crearUsuario(String username, String passwordPlano, String nombre, Set<Rol> roles) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(passwordPlano));
        u.setNombre(nombre);
        u.setActivo(true);
        u.setRoles(roles);
        usuarioRepository.save(u);
    }
}