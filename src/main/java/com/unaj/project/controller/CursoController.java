package com.unaj.project.controller;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.ProfesorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listar(Model model) {
        model.addAttribute("cursos", cursoService.listarTodos());
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cursoForm", new CursoForm());
        model.addAttribute("profesores", profesorService.listarTodos());
        return "cursos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cursoForm") CursoForm cursoForm,
                          BindingResult result,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("profesores", profesorService.listarTodos());
            return "cursos/formulario";
        }
        cursoService.guardar(cursoForm);
        ra.addFlashAttribute("mensajeExito", "Curso guardado correctamente.");
        return "redirect:/cursos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cursoForm", cursoService.buscarFormPorId(id));
        model.addAttribute("profesores", profesorService.listarTodos());
        return "cursos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        cursoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Curso eliminado correctamente.");
        return "redirect:/cursos";
    }
}