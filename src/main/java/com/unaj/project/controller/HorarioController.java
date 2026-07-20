// src/main/java/com/unaj/project/controller/HorarioController.java
package com.unaj.project.controller;

import com.unaj.project.model.*;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.HorarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // Grilla completa del ciclo: 6 días x 3 turnos
    @GetMapping
    public String listar(@RequestParam(required = false) Long cicloId, Model model) {
        Ciclo cicloSel = (cicloId != null) ? cicloService.buscarPorId(cicloId) : cicloService.obtenerActivo();

        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("cicloSel", cicloSel);

        if (cicloSel != null) {
            model.addAttribute("grilla", horarioService.agruparParaGrilla(cicloSel.getId()));
        }
        return "horarios/lista";
    }

    // Decide entre "nueva jornada" (pide horas + cursos) o "agregar curso" (la jornada ya existe)
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam Long cicloId, @RequestParam DiaSemana dia, @RequestParam Turno turno,
                        Model model) {
        prepararForm(model, cicloId, dia, turno);
        return "horarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long cicloId,
                          @RequestParam DiaSemana dia,
                          @RequestParam Turno turno,
                          @RequestParam(required = false) String horaInicio,
                          @RequestParam(required = false) String horaFin,
                          @RequestParam(required = false) List<Long> cursoIds,
                          Model model,
                          RedirectAttributes ra) {
        try {
            horarioService.guardarJornada(cicloId, dia, turno,
                    horaInicio != null && !horaInicio.isBlank() ? LocalTime.parse(horaInicio) : null,
                    horaFin != null && !horaFin.isBlank() ? LocalTime.parse(horaFin) : null,
                    cursoIds);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararForm(model, cicloId, dia, turno);
            return "horarios/formulario";
        }
        ra.addFlashAttribute("mensajeExito", "Horario guardado correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/{jornadaId}/editar-horas")
    public String editarHoras(@PathVariable Long jornadaId,
                              @RequestParam String horaInicio,
                              @RequestParam String horaFin,
                              @RequestParam Long cicloId,
                              RedirectAttributes ra) {
        horarioService.editarHoras(jornadaId, LocalTime.parse(horaInicio), LocalTime.parse(horaFin));
        ra.addFlashAttribute("mensajeExito", "Horario actualizado correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/{jornadaId}/quitar-curso/{horarioId}")
    public String quitarCurso(@PathVariable Long jornadaId, @PathVariable Long horarioId,
                              @RequestParam Long cicloId, RedirectAttributes ra) {
        horarioService.quitarCurso(horarioId);
        ra.addFlashAttribute("mensajeExito", "Curso quitado del horario.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/eliminar/{jornadaId}")
    public String eliminar(@PathVariable Long jornadaId, @RequestParam Long cicloId, RedirectAttributes ra) {
        horarioService.eliminarJornada(jornadaId);
        ra.addFlashAttribute("mensajeExito", "Horario eliminado correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    private void prepararForm(Model model, Long cicloId, DiaSemana dia, Turno turno) {
        Jornada jornada = horarioService.buscarJornada(cicloId, dia, turno);
        model.addAttribute("jornada", jornada);
        model.addAttribute("ciclo", cicloService.buscarPorId(cicloId));
        model.addAttribute("cicloId", cicloId);
        model.addAttribute("dia", dia);
        model.addAttribute("turno", turno);
        model.addAttribute("cursos", cursoService.listarTodos());

        List<Long> yaAgregados = (jornada != null)
                ? jornada.getHorarios().stream().map(h -> h.getCurso().getId()).collect(Collectors.toList())
                : List.of();
        model.addAttribute("cursoIdsAgregados", yaAgregados);
    }
}
