package com.unaj.project.controller;

import com.unaj.project.dto.EventoAcademicoForm;
import com.unaj.project.service.EventoAcademicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/calendario")
public class EventoAcademicoController {

    private final EventoAcademicoService eventoAcademicoService;

    public EventoAcademicoController(EventoAcademicoService eventoAcademicoService) {
        this.eventoAcademicoService = eventoAcademicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("eventos", eventoAcademicoService.listarTodos());
        return "calendario/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("eventoForm", new EventoAcademicoForm());
        return "calendario/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("eventoForm", eventoAcademicoService.buscarFormPorId(id));
        return "calendario/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("eventoForm") EventoAcademicoForm eventoForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "calendario/formulario";
        }
        eventoAcademicoService.guardar(eventoForm);
        ra.addFlashAttribute("mensajeExito", "Evento guardado correctamente.");
        return "redirect:/calendario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        eventoAcademicoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Evento eliminado correctamente.");
        return "redirect:/calendario";
    }
}
