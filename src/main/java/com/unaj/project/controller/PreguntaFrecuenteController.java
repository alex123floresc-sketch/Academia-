package com.unaj.project.controller;

import com.unaj.project.dto.PreguntaFrecuenteForm;
import com.unaj.project.service.PreguntaFrecuenteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/faq")
public class PreguntaFrecuenteController {

    private final PreguntaFrecuenteService preguntaFrecuenteService;

    public PreguntaFrecuenteController(PreguntaFrecuenteService preguntaFrecuenteService) {
        this.preguntaFrecuenteService = preguntaFrecuenteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("preguntas", preguntaFrecuenteService.listarTodas());
        return "faq/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("preguntaForm", new PreguntaFrecuenteForm());
        return "faq/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("preguntaForm", preguntaFrecuenteService.buscarFormPorId(id));
        return "faq/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("preguntaForm") PreguntaFrecuenteForm preguntaForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "faq/formulario";
        }
        preguntaFrecuenteService.guardar(preguntaForm);
        ra.addFlashAttribute("mensajeExito", "Pregunta guardada correctamente.");
        return "redirect:/faq";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        preguntaFrecuenteService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Pregunta eliminada correctamente.");
        return "redirect:/faq";
    }
}
