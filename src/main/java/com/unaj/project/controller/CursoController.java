package com.unaj.project.controller;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Nivel;
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
import java.util.List;

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
    public String listar(@RequestParam(required = false) Nivel nivel,
                         @RequestParam(required = false) String area,
                         @RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        boolean hayBusqueda = q != null && !q.isBlank();

        if (nivel == null && !hayBusqueda) {
            model.addAttribute("resumenNiveles", cursoService.contarPorNivel());
            return "cursos/niveles";
        }

        String areaSel = null;
        if (nivel != null) {
            List<String> areasDelNivel = Areas.paraNivel(nivel);
            areaSel = (area != null && areasDelNivel.contains(area)) ? area : null;
            if (areaSel == null && !hayBusqueda) {
                model.addAttribute("nivel", nivel);
                model.addAttribute("resumenAreas", cursoService.contarPorArea(nivel));
                return "cursos/areas";
            }
        }

        Page<Curso> pagina = cursoService.buscarPagina(q, nivel, areaSel, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("cursos", pagina.getContent());
        model.addAttribute("q", q);
        model.addAttribute("nivel", nivel);
        model.addAttribute("area", areaSel);
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Nivel nivel, Model model) {
        CursoForm form = new CursoForm();
        form.setNivel(nivel);
        model.addAttribute("cursoForm", form);
        model.addAttribute("profesores", profesorService.listarTodos());
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("area", (String) null);
        model.addAttribute("nivelContexto", nivel);
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
            model.addAttribute("niveles", Nivel.values());
            model.addAttribute("area", area);
            model.addAttribute("nivelContexto", cursoForm.getNivel());
            return "cursos/formulario";
        }
        cursoService.guardar(cursoForm);
        ra.addFlashAttribute("mensajeExito", "Curso guardado correctamente.");
        return "redirect:/cursos" + volverA(cursoForm.getNivel(), area);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam(required = false) String area, Model model) {
        CursoForm form = cursoService.buscarFormPorId(id);
        model.addAttribute("cursoForm", form);
        model.addAttribute("profesores", profesorService.listarTodos());
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("area", area);
        model.addAttribute("nivelContexto", form.getNivel());
        return "cursos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, @RequestParam(required = false) Nivel nivel,
                           @RequestParam(required = false) String area, RedirectAttributes ra) {
        cursoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Curso eliminado correctamente.");
        return "redirect:/cursos" + volverA(nivel, area);
    }

    private String volverA(Nivel nivel, String area) {
        if (nivel == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?nivel=").append(nivel.name());
        if (area != null && !area.isBlank()) {
            sb.append("&area=").append(URLEncoder.encode(area, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
