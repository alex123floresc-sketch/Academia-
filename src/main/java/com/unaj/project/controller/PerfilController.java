package com.unaj.project.controller;

import com.unaj.project.dto.PerfilForm;
import com.unaj.project.model.Usuario;
import com.unaj.project.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public String ver(Authentication auth, Model model) {
        Usuario usuario = perfilService.obtenerPropio(auth.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilForm", perfilService.formPropio(auth.getName()));
        return "perfil/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("perfilForm") PerfilForm form,
                          BindingResult result,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("usuario", perfilService.obtenerPropio(auth.getName()));
            return "perfil/formulario";
        }
        perfilService.actualizarPropio(auth.getName(), form);
        ra.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
        return "redirect:/perfil";
    }

    @GetMapping("/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(Authentication auth) {
        Usuario usuario = perfilService.obtenerPropio(auth.getName());
        if (!usuario.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (usuario.getFotoContentType() != null)
                ? MediaType.parseMediaType(usuario.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(usuario.getFoto());
    }

    @GetMapping("/firma")
    @ResponseBody
    public ResponseEntity<byte[]> firma(Authentication auth) {
        Usuario usuario = perfilService.obtenerPropio(auth.getName());
        return firmaDe(usuario);
    }

    @GetMapping("/firma/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> firmaDeUsuario(@PathVariable Long id) {
        Usuario usuario = perfilService.obtenerPorId(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return firmaDe(usuario);
    }

    private ResponseEntity<byte[]> firmaDe(Usuario usuario) {
        if (!usuario.isFirmaPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (usuario.getFirmaContentType() != null)
                ? MediaType.parseMediaType(usuario.getFirmaContentType())
                : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().contentType(tipo).body(usuario.getFirma());
    }
}
