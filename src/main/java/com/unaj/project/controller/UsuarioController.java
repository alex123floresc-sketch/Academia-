package com.unaj.project.controller;

import com.unaj.project.dto.EstadoSolicitudEliminacionDTO;
import com.unaj.project.dto.UsuarioForm;
import com.unaj.project.model.SolicitudEliminacionUsuario;
import com.unaj.project.model.Usuario;
import com.unaj.project.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

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
                         Authentication auth,
                         Model model) {
        Page<Usuario> pagina = usuarioService.buscarPagina(q, pageable);
        String usernameActual = auth != null ? auth.getName() : null;
        int requeridas = usuarioService.aprobacionesRequeridas();
        Map<Long, EstadoSolicitudEliminacionDTO> estadosSolicitud = usuarioService.solicitudesPendientesPorObjetivo()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    SolicitudEliminacionUsuario s = e.getValue();
                    boolean yaAprobo = s.getAprobadoPor().stream()
                            .anyMatch(u -> u.getUsername().equals(usernameActual));
                    return new EstadoSolicitudEliminacionDTO(s.getAprobadoPor().size(), requeridas, yaAprobo);
                }));
        model.addAttribute("pagina", pagina);
        model.addAttribute("usuarios", pagina.getContent());
        model.addAttribute("q", q);
        model.addAttribute("estadosSolicitud", estadosSolicitud);
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
    public String eliminar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        UsuarioService.ResultadoEliminacion resultado = usuarioService.eliminar(id, auth.getName());
        switch (resultado) {
            case ELIMINADO -> ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
            case SOLICITUD_CREADA -> ra.addFlashAttribute("mensajeExito",
                    "Es una cuenta de administrador: se creó una solicitud de eliminación. Hacen falta "
                            + (usuarioService.aprobacionesRequeridas() - 1) + " aprobación(es) más de otros administradores.");
            case APROBACION_REGISTRADA -> ra.addFlashAttribute("mensajeExito", "Tu aprobación quedó registrada.");
            case YA_APROBADO -> ra.addFlashAttribute("mensajeError", "Ya habías aprobado esta solicitud de eliminación.");
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}/cancelar")
    public String cancelarSolicitud(@PathVariable Long id, RedirectAttributes ra) {
        usuarioService.cancelarSolicitudEliminacion(id);
        ra.addFlashAttribute("mensajeExito", "Solicitud de eliminación cancelada.");
        return "redirect:/usuarios";
    }
}
