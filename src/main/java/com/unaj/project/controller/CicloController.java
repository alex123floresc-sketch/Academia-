package com.unaj.project.controller;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.service.CicloService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ciclos")
public class CicloController {

    private final CicloService cicloService;

    public CicloController(CicloService cicloService) {
        this.cicloService = cicloService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ciclos", cicloService.listarTodos());
        return "ciclos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cicloForm", new CicloForm());
        return "ciclos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cicloForm") CicloForm cicloForm,
                          BindingResult result) {
        // Validación cruzada: la fecha de fin debe ser posterior a la de inicio
        if (cicloForm.getFechaInicio() != null && cicloForm.getFechaFin() != null
                && !cicloForm.getFechaFin().isAfter(cicloForm.getFechaInicio())) {
            result.rejectValue("fechaFin", "error.fechaFin",
                    "La fecha de fin debe ser posterior a la de inicio.");
        }
        if (result.hasErrors()) {
            return "ciclos/formulario";
        }
        cicloService.guardar(cicloForm);
        return "redirect:/ciclos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cicloForm", cicloService.buscarFormPorId(id));
        return "ciclos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        cicloService.eliminar(id);
        return "redirect:/ciclos";
    }
}