package com.unaj.project.controller;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.model.Ciclo;
import com.unaj.project.service.CicloService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ciclos")
public class CicloController {

    private final CicloService cicloService;

    public CicloController(CicloService cicloService) {
        this.cicloService = cicloService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        Page<Ciclo> pagina = cicloService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("ciclos", pagina.getContent());
        model.addAttribute("q", q);
        return "ciclos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cicloForm", new CicloForm());
        return "ciclos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cicloForm") CicloForm cicloForm,
                          BindingResult result,
                          RedirectAttributes ra) {
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
        ra.addFlashAttribute("mensajeExito", "Ciclo guardado correctamente.");
        return "redirect:/ciclos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cicloForm", cicloService.buscarFormPorId(id));
        return "ciclos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        cicloService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Ciclo eliminado correctamente.");
        return "redirect:/ciclos";
    }
}