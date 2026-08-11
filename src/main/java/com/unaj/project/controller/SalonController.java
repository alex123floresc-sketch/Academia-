package com.unaj.project.controller;

import com.unaj.project.dto.SalonForm;
import com.unaj.project.service.SalonService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/salones")
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("salones", salonService.listarTodos());
        return "salones/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("salonForm", new SalonForm());
        return "salones/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("salonForm", salonService.buscarFormPorId(id));
        return "salones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("salonForm") SalonForm salonForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "salones/formulario";
        }
        salonService.guardar(salonForm);
        ra.addFlashAttribute("mensajeExito", "Salón guardado correctamente.");
        return "redirect:/salones";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        salonService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Salón eliminado correctamente.");
        return "redirect:/salones";
    }
}
