package com.unaj.project.controller;

import com.unaj.project.dto.PasoAdmisionForm;
import com.unaj.project.service.PasoAdmisionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pasos-admision")
public class PasoAdmisionController {

    private final PasoAdmisionService pasoAdmisionService;

    public PasoAdmisionController(PasoAdmisionService pasoAdmisionService) {
        this.pasoAdmisionService = pasoAdmisionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pasos", pasoAdmisionService.listarTodos());
        return "pasos-admision/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pasoForm", new PasoAdmisionForm());
        return "pasos-admision/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pasoForm", pasoAdmisionService.buscarFormPorId(id));
        return "pasos-admision/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("pasoForm") PasoAdmisionForm pasoForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "pasos-admision/formulario";
        }
        pasoAdmisionService.guardar(pasoForm);
        ra.addFlashAttribute("mensajeExito", "Paso guardado correctamente.");
        return "redirect:/pasos-admision";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        pasoAdmisionService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Paso eliminado correctamente.");
        return "redirect:/pasos-admision";
    }
}
