// src/main/java/com/unaj/project/controller/HorarioController.java
package com.unaj.project.controller;

import com.unaj.project.model.*;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/horarios")
public class HorarioController {

    private final HorarioService horarioService;
    private final CicloService cicloService;
    private final CursoService cursoService;

    public HorarioController(HorarioService horarioService,
                             CicloService cicloService,
                             CursoService cursoService) {
        this.horarioService = horarioService;
        this.cicloService = cicloService;
        this.cursoService = cursoService;
    }
    // Grilla filtrable por ciclo + turno
    @GetMapping
    public String listar(@RequestParam(required = false) Long cicloId,
                         @RequestParam(required = false) Turno turno,
                         Model model) {

        Ciclo cicloSel = (cicloId != null) ? cicloService.buscarPorId(cicloId)
                : cicloService.obtenerActivo();
        Turno turnoSel = (turno != null) ? turno : Turno.MANANA;

        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("cicloSel", cicloSel);
        model.addAttribute("turnoSel", turnoSel);

        if (cicloSel != null) {
            model.addAttribute("horariosPorDia",
                    horarioService.agruparPorDia(cicloSel.getId(), turnoSel));
        }
        return "horarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        prepararForm(model, new Horario());
        return "horarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        prepararForm(model, horarioService.buscarPorId(id));
        return "horarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long cursoId,
                          @RequestParam Long cicloId,
                          @RequestParam Turno turno,
                          @RequestParam DiaSemana diaSemana,
                          @RequestParam String horaInicio,
                          @RequestParam String horaFin,
                          @RequestParam(required = false) String aula,
                          Model model) {

        Horario h = (id != null) ? horarioService.buscarPorId(id) : new Horario();
        h.setCurso(cursoService.buscarPorId(cursoId));
        h.setCiclo(cicloService.buscarPorId(cicloId));
        h.setTurno(turno);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(java.time.LocalTime.parse(horaInicio));
        h.setHoraFin(java.time.LocalTime.parse(horaFin));
        h.setAula(aula);

        try {
            horarioService.guardar(h);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararForm(model, h);
            return "horarios/formulario";
        }
        return "redirect:/horarios?cicloId=" + cicloId + "&turno=" + turno;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return "redirect:/horarios";
    }

    private void prepararForm(Model model, Horario horario) {
        model.addAttribute("horario", horario);
        model.addAttribute("cursos", cursoService.listarTodos());
        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
    }
}