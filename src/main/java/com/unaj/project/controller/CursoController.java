package com.unaj.project.controller;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Areas;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.ProfesorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;
    private final ProfesorService profesorService;

    public CursoController(CursoService cursoService, ProfesorService profesorService) {
        this.cursoService = cursoService;
        this.profesorService = profesorService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String area,
                         @RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        String areaSel = (area != null && Areas.TODAS.contains(area)) ? area : null;

        if (areaSel == null && (q == null || q.isBlank())) {
            model.addAttribute("resumenAreas", cursoService.contarPorArea());
            return "cursos/areas";
        }

        Page<com.unaj.project.model.Curso> pagina = cursoService.buscarPagina(q, areaSel, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("cursos", pagina.getContent());
        model.addAttribute("q", q);
        model.addAttribute("area", areaSel);
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) String area, Model model) {
        model.addAttribute("cursoForm", new CursoForm());
        model.addAttribute("profesores", profesorService.listarTodos());
        model.addAttribute("area", area);
        return "cursos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cursoForm") CursoForm cursoForm,
                          BindingResult result,
                          @RequestParam(required = false) String area,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("profesores", profesorService.listarTodos());
            model.addAttribute("area", area);
            return "cursos/formulario";
        }
        cursoService.guardar(cursoForm);
        ra.addFlashAttribute("mensajeExito", "Curso guardado correctamente.");
        return "redirect:/cursos" + volverAArea(area);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam(required = false) String area, Model model) {
        model.addAttribute("cursoForm", cursoService.buscarFormPorId(id));
        model.addAttribute("profesores", profesorService.listarTodos());
        model.addAttribute("area", area);
        return "cursos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, @RequestParam(required = false) String area, RedirectAttributes ra) {
        cursoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Curso eliminado correctamente.");
        return "redirect:/cursos" + volverAArea(area);
    }

    private String volverAArea(String area) {
        return (area != null && !area.isBlank())
                ? "?area=" + URLEncoder.encode(area, StandardCharsets.UTF_8)
                : "";
    }
}