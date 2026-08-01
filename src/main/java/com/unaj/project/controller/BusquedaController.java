package com.unaj.project.controller;

import com.unaj.project.model.Alumno;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Profesor;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.ProfesorService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BusquedaController {

    private static final int LIMITE_POR_CATEGORIA = 8;

    private final AlumnoService alumnoService;
    private final ProfesorService profesorService;
    private final CursoService cursoService;

    public BusquedaController(AlumnoService alumnoService, ProfesorService profesorService, CursoService cursoService) {
        this.alumnoService = alumnoService;
        this.profesorService = profesorService;
        this.cursoService = cursoService;
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String q, Authentication auth, Model model) {
        model.addAttribute("q", q);

        boolean puedeVerGestion = tieneAlgunRol(auth, "ROLE_ADMIN", "ROLE_CAJERO");

        if (q != null && !q.isBlank()) {
            model.addAttribute("alumnos", alumnoService.buscarPagina(q, PageRequest.of(0, LIMITE_POR_CATEGORIA)).getContent());
            if (puedeVerGestion) {
                model.addAttribute("profesores", profesorService.buscarPagina(q, PageRequest.of(0, LIMITE_POR_CATEGORIA)).getContent());
                model.addAttribute("cursos", cursoService.buscarPagina(q, PageRequest.of(0, LIMITE_POR_CATEGORIA)).getContent());
            } else {
                model.addAttribute("profesores", List.<Profesor>of());
                model.addAttribute("cursos", List.<Curso>of());
            }
        } else {
            model.addAttribute("alumnos", List.<Alumno>of());
            model.addAttribute("profesores", List.<Profesor>of());
            model.addAttribute("cursos", List.<Curso>of());
        }
        return "busqueda/resultados";
    }

    private boolean tieneAlgunRol(Authentication auth, String... roles) {
        if (auth == null) {
            return false;
        }
        List<String> buscados = List.of(roles);
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (buscados.contains(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
