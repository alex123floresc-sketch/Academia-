package com.unaj.project.controller;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.dto.RegistroAsistenciaRequest;
import com.unaj.project.dto.RegistroIngresoRequest;
import com.unaj.project.model.Ciclo;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.RegistroHoras;
import com.unaj.project.model.Turno;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.AsistenciaService;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.RegistroHorasService;
import com.unaj.project.service.RegistroIngresoService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    private final RegistroIngresoService registroIngresoService;
    private final AsistenciaService asistenciaService;
    private final CicloService cicloService;
    private final HorarioService horarioService;
    private final HorarioRepository horarioRepository;
    private final RegistroHorasService registroHorasService;

    public AsistenciaController(RegistroIngresoService registroIngresoService,
                                AsistenciaService asistenciaService,
                                CicloService cicloService,
                                HorarioService horarioService,
                                HorarioRepository horarioRepository,
                                RegistroHorasService registroHorasService) {
        this.registroIngresoService = registroIngresoService;
        this.asistenciaService = asistenciaService;
        this.cicloService = cicloService;
        this.horarioService = horarioService;
        this.horarioRepository = horarioRepository;
        this.registroHorasService = registroHorasService;
    }

    // Hub: elegir entre asistencia de estudiantes o de docentes

    @GetMapping
    public String inicio() {
        return "asistencias/inicio";
    }

    // Registro de entrada a la institución (estudiantes, uso principal, pensado para auxiliares)

    @GetMapping("/estudiantes")
    public String ingreso(Model model) {
        model.addAttribute("registrosHoy", registroIngresoService.listarDeHoy());
        model.addAttribute("totalHoy", registroIngresoService.contarDeHoy());
        model.addAttribute("hoy", LocalDate.now());
        return "asistencias/ingreso";
    }

    // Registro de llegada de docentes, según el curso que les toca dictar hoy por su horario

    @GetMapping("/docentes")
    public String docentes(@RequestParam(required = false) Turno turno, Model model) {
        Ciclo cicloActivo = cicloService.obtenerActivo();
        DiaSemana diaHoy = DiaSemana.desde(LocalDate.now().getDayOfWeek());
        Turno turnoSel = (turno != null) ? turno : turnoActual();

        List<Horario> horariosHoy = List.of();
        if (cicloActivo != null && diaHoy != null) {
            horariosHoy = horarioRepository.findByCicloId(cicloActivo.getId()).stream()
                    .filter(h -> h.getDiaSemana() == diaHoy && h.getTurno() == turnoSel)
                    .sorted(Comparator.comparing(Horario::getHoraInicio)
                            .thenComparing(h -> h.getCurso().getProfesor() != null
                                    ? h.getCurso().getProfesor().getNombreCompleto() : ""))
                    .toList();
        }

        Map<Long, RegistroHoras> registrosHoy = registroHorasService.buscarDeHoyPorHorarios(
                horariosHoy.stream().map(Horario::getId).toList());

        model.addAttribute("ciclo", cicloActivo);
        model.addAttribute("diaHoy", diaHoy);
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("turnoSel", turnoSel);
        model.addAttribute("horarios", horariosHoy);
        model.addAttribute("registrosHoy", registrosHoy);
        return "asistencias/docentes";
    }

    @PostMapping("/docentes/marcar")
    public String docentesMarcar(@RequestParam Long horarioId,
                                 @RequestParam(required = false) Turno turno,
                                 Authentication auth, RedirectAttributes ra) {
        String username = (auth != null) ? auth.getName() : null;
        registroHorasService.marcarLlegada(horarioId, LocalDate.now(), username);
        ra.addFlashAttribute("mensajeExito", "Llegada del docente registrada.");
        return "redirect:/asistencias/docentes" + (turno != null ? "?turno=" + turno : "");
    }

    private Turno turnoActual() {
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(LocalTime.of(13, 0))) return Turno.MANANA;
        if (ahora.isBefore(LocalTime.of(19, 0))) return Turno.TARDE;
        return Turno.NOCHE;
    }

    @PostMapping(value = "/registrar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AsistenciaResultadoDTO registrar(@RequestBody RegistroIngresoRequest body, Authentication auth) {
        String username = (auth != null) ? auth.getName() : null;
        return registroIngresoService.registrar(body.codigo(), username);
    }

    // Asistencia por curso (histórico, ligado a los horarios de clase)

    @GetMapping("/cursos")
    public String cursosLista(@RequestParam(required = false) Long cicloId,
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
        return "asistencias/cursos-lista";
    }

    @GetMapping("/cursos/escanear/{horarioId}")
    public String cursosEscanear(@PathVariable Long horarioId, Model model) {
        Horario horario = horarioService.buscarPorId(horarioId);
        model.addAttribute("horario", horario);
        model.addAttribute("registrosHoy", asistenciaService.listarDeHoy(horarioId));
        return "asistencias/cursos-escanear";
    }

    @PostMapping(value = "/cursos/registrar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AsistenciaResultadoDTO cursosRegistrar(@RequestBody RegistroAsistenciaRequest body, Authentication auth) {
        String username = (auth != null) ? auth.getName() : null;
        return asistenciaService.registrar(body.horarioId(), body.codigo(), username);
    }
}
