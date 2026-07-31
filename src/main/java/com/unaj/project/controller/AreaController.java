package com.unaj.project.controller;

import com.unaj.project.model.Areas;
import com.unaj.project.service.CursoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/areas")
public class AreaController {

    private final CursoService cursoService;

    public AreaController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) String area, Model model) {
        String seleccionada = (area != null && Areas.TODAS.contains(area)) ? area : Areas.TODAS.get(0);
        model.addAttribute("areas", Areas.TODAS);
        model.addAttribute("areaSeleccionada", seleccionada);
        model.addAttribute("cursos", cursoService.listarTodos());
        return "areas/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String area,
                          @RequestParam(required = false) List<Long> cursoIds,
                          RedirectAttributes ra) {
        cursoService.establecerCursosDeArea(area, cursoIds);
        ra.addFlashAttribute("mensajeExito", "Cursos de " + area + " actualizados correctamente.");
        return "redirect:/areas?area=" + area;
    }
}
