package com.unaj.project.controller;

import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.Usuario;
import com.unaj.project.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final String USERNAME_DESARROLLADOR = "desarrollador";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Authentication auth,
                         Model model) {
        String usernameActual = auth != null ? auth.getName() : null;
        Page<Usuario> pagina = usuarioService.buscarPagina(q, usernameActual, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("usuarios", pagina.getContent());
        model.addAttribute("q", q);
        model.addAttribute("esDesarrollador", USERNAME_DESARROLLADOR.equals(usernameActual));
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Authentication auth, Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("roles", usuarioService.listarRolesAsignablesPor(auth.getName()));
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("usuarioForm", usuarioService.buscarFormPorId(id, auth.getName()));
        model.addAttribute("roles", usuarioService.listarRolesAsignablesPor(auth.getName()));
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioForm usuarioForm,
                          BindingResult result,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {
        boolean esNuevo = (usuarioForm.getId() == null);
        boolean sinPassword = (usuarioForm.getPasswordPlano() == null || usuarioForm.getPasswordPlano().isBlank());
        if (esNuevo && sinPassword) {
            result.rejectValue("passwordPlano", "error.passwordPlano",
                    "La contraseña es obligatoria al crear un usuario.");
        }
        if (result.hasErrors()) {
            model.addAttribute("roles", usuarioService.listarRolesAsignablesPor(auth.getName()));
            return "usuarios/formulario";
        }
        usuarioService.guardar(usuarioForm, auth.getName());
        ra.addFlashAttribute("mensajeExito", "Usuario guardado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        usuarioService.eliminar(id, auth.getName());
        ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(@PathVariable Long id, Authentication auth) {
        Usuario usuario = usuarioService.buscarVisiblePorId(id, auth.getName());
        if (!usuario.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (usuario.getFotoContentType() != null)
                ? MediaType.parseMediaType(usuario.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(usuario.getFoto());
    }

}
