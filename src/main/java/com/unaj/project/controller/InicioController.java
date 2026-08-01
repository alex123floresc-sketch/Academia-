package com.unaj.project.controller;

import com.unaj.project.model.Usuario;
import com.unaj.project.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;

    public InicioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/inicio")
    public String inicio(Authentication auth, Model model) {
        String nombre = null;
        if (auth != null) {
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            nombre = (usuario != null) ? usuario.getNombre() : null;
        }
        model.addAttribute("nombreUsuario", nombre);
        model.addAttribute("hoy", LocalDate.now());
        return "inicio";
    }
}
