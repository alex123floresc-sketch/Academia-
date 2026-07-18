package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final PagoRepository pagoRepository;

    public AlumnoController(AlumnoService alumnoService, PagoRepository pagoRepository) {
        this.alumnoService = alumnoService;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        java.util.Map<Long, Long> deuda = new java.util.HashMap<>();
        for (Object[] fila : pagoRepository.contarDeudaPorAlumno()) {
            deuda.put((Long) fila[0], (Long) fila[1]);
        }
        model.addAttribute("alumnos", alumnoService.listarTodos());
        model.addAttribute("deuda", deuda);
        return "alumnos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("alumnoForm", new AlumnoForm());
        return "alumnos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("alumnoForm") AlumnoForm alumnoForm,
                          BindingResult result) {
        if (result.hasErrors()) {
            return "alumnos/formulario";   // remuestra con los mensajes de error
        }
        alumnoService.guardar(alumnoForm);
        return "redirect:/alumnos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("alumnoForm", alumnoService.buscarFormPorId(id));
        return "alumnos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
        return "redirect:/alumnos";
    }
}