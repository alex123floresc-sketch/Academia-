package com.unaj.project.controller;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.service.ProfesorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profesores")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("profesores", profesorService.listarTodos());
        return "profesores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("profesorForm", new ProfesorForm());
        return "profesores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("profesorForm") ProfesorForm profesorForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "profesores/formulario";
        }
        profesorService.guardar(profesorForm);
        ra.addFlashAttribute("mensajeExito", "Profesor guardado correctamente.");
        return "redirect:/profesores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("profesorForm", profesorService.buscarFormPorId(id));
        return "profesores/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        profesorService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Profesor eliminado correctamente.");
        return "redirect:/profesores";
    }
}