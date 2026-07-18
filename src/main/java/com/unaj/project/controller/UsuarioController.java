package com.unaj.project.controller;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("roles", usuarioService.listarRoles());
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuarioForm", usuarioService.buscarFormPorId(id));
        model.addAttribute("roles", usuarioService.listarRoles());
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioForm usuarioForm,
                          BindingResult result,
                          Model model) {
        // Contraseña obligatoria solo al crear (id == null)
        boolean esNuevo = (usuarioForm.getId() == null);
        boolean sinPassword = (usuarioForm.getPasswordPlano() == null || usuarioForm.getPasswordPlano().isBlank());
        if (esNuevo && sinPassword) {
            result.rejectValue("passwordPlano", "error.passwordPlano",
                    "La contraseña es obligatoria al crear un usuario.");
        }
        if (result.hasErrors()) {
            model.addAttribute("roles", usuarioService.listarRoles());
            return "usuarios/formulario";
        }
        usuarioService.guardar(usuarioForm);
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return "redirect:/usuarios";
    }
}