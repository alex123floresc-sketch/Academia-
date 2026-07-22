package com.unaj.project.controller;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Usuario;
import com.unaj.project.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        Page<Usuario> pagina = usuarioService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("usuarios", pagina.getContent());
        model.addAttribute("q", q);
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
                          Model model,
                          RedirectAttributes ra) {
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
        ra.addFlashAttribute("mensajeExito", "Usuario guardado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        usuarioService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        return "redirect:/usuarios";
    }
}