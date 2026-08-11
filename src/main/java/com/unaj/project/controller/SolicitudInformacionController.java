package com.unaj.project.controller;

import com.unaj.project.dto.SolicitudInformacionForm;
import com.unaj.project.model.Nivel;
import com.unaj.project.service.SolicitudInformacionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SolicitudInformacionController {

    private final SolicitudInformacionService solicitudInformacionService;

    public SolicitudInformacionController(SolicitudInformacionService solicitudInformacionService) {
        this.solicitudInformacionService = solicitudInformacionService;
    }

    /** Envío público del formulario "Solicita información" de la página web, sin login. */
    @PostMapping("/solicitud-informacion")
    public String enviar(@Valid @ModelAttribute("solicitudForm") SolicitudInformacionForm solicitudForm,
                         BindingResult result,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("mensajeErrorContacto", "Revisa los datos ingresados: " +
                    result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/#contacto-form";
        }
        solicitudInformacionService.registrar(solicitudForm);
        ra.addFlashAttribute("mensajeExitoContacto", "¡Gracias! Recibimos tu solicitud, nos pondremos en contacto contigo pronto.");
        return "redirect:/#contacto-form";
    }

    @GetMapping("/solicitudes")
    public String listar(Model model) {
        model.addAttribute("solicitudes", solicitudInformacionService.listarTodas());
        model.addAttribute("niveles", Nivel.values());
        return "solicitudes/lista";
    }

    @PostMapping("/solicitudes/{id}/atender")
    public String atender(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean atendida, RedirectAttributes ra) {
        solicitudInformacionService.marcarAtendida(id, atendida);
        ra.addFlashAttribute("mensajeExito", atendida ? "Solicitud marcada como atendida." : "Solicitud marcada como pendiente.");
        return "redirect:/solicitudes";
    }
}
