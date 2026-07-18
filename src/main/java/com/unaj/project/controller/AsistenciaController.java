package com.unaj.project.controller;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.dto.RegistroAsistenciaRequest;
import com.unaj.project.model.Ciclo;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Turno;
import com.unaj.project.service.AsistenciaService;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.HorarioService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final CicloService cicloService;
    private final HorarioService horarioService;

    public AsistenciaController(AsistenciaService asistenciaService, CicloService cicloService,
                                HorarioService horarioService) {
        this.asistenciaService = asistenciaService;
        this.cicloService = cicloService;
        this.horarioService = horarioService;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Long cicloId,
                        @RequestParam(required = false) Turno turno,
                        @RequestParam(required = false) DiaSemana dia,
                        Model model) {

        Ciclo cicloSel = (cicloId != null) ? cicloService.buscarPorId(cicloId) : cicloService.obtenerActivo();
        Turno turnoSel = (turno != null) ? turno : Turno.MANANA;
        DiaSemana diaSel = (dia != null) ? dia : DiaSemana.desde(LocalDate.now().getDayOfWeek());

        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("cicloSel", cicloSel);
        model.addAttribute("turnoSel", turnoSel);
        model.addAttribute("diaSel", diaSel);

        List<Horario> horarios = (cicloSel != null)
                ? horarioService.listarPorCicloTurnoDia(cicloSel.getId(), turnoSel, diaSel)
                : List.of();

        Map<Long, Long> conteos = new LinkedHashMap<>();
        for (Horario h : horarios) {
            conteos.put(h.getId(), asistenciaService.contarDeHoy(h.getId()));
        }

        model.addAttribute("horarios", horarios);
        model.addAttribute("conteos", conteos);
        return "asistencias/lista";
    }

    @GetMapping("/escanear/{horarioId}")
    public String escanear(@PathVariable Long horarioId, Model model) {
        Horario horario = horarioService.buscarPorId(horarioId);
        model.addAttribute("horario", horario);
        model.addAttribute("registrosHoy", asistenciaService.listarDeHoy(horarioId));
        return "asistencias/escanear";
    }

    @PostMapping(value = "/registrar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AsistenciaResultadoDTO registrar(@RequestBody RegistroAsistenciaRequest body, Authentication auth) {
        String username = (auth != null) ? auth.getName() : null;
        return asistenciaService.registrar(body.horarioId(), body.codigo(), username);
    }
}
