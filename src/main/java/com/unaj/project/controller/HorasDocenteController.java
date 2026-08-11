package com.unaj.project.controller;

import com.unaj.project.model.Horario;
import com.unaj.project.model.PagoProfesor;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.RegistroHoras;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.PagoProfesorService;
import com.unaj.project.service.ProfesorService;
import com.unaj.project.service.RegistroHorasService;
import com.unaj.project.util.PeriodoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/horas-docentes")
public class HorasDocenteController {

    private final ProfesorService profesorService;
    private final HorarioRepository horarioRepository;
    private final HorarioService horarioService;
    private final RegistroHorasService registroHorasService;
    private final PagoProfesorService pagoProfesorService;

    public HorasDocenteController(ProfesorService profesorService,
                                  HorarioRepository horarioRepository,
                                  HorarioService horarioService,
                                  RegistroHorasService registroHorasService,
                                  PagoProfesorService pagoProfesorService) {
        this.profesorService = profesorService;
        this.horarioRepository = horarioRepository;
        this.horarioService = horarioService;
        this.registroHorasService = registroHorasService;
        this.pagoProfesorService = pagoProfesorService;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) Long profesorId,
                      @RequestParam(required = false) LocalDate semana,
                      @RequestParam(required = false) LocalDate quincena,
                      @RequestParam(required = false) String q,
                      @PageableDefault(size = 12) Pageable pageable,
                      Model model) {
        model.addAttribute("profesores", profesorService.listarTodos());
        model.addAttribute("profesorId", profesorId);

        if (profesorId == null) {
            Page<Profesor> pagina = profesorService.buscarPagina(q, pageable);
            model.addAttribute("pagina", pagina);
            model.addAttribute("q", q);
            return "horas-docentes/lista";
        }

        Profesor profesor = profesorService.buscarPorId(profesorId);
        List<Horario> horarios = horarioRepository.findByCursoProfesorId(profesorId).stream()
                .sorted(Comparator.comparing(Horario::getDiaSemana)
                        .thenComparing(Horario::getTurno)
                        .thenComparing(Horario::getHoraInicio))
                .toList();

        PeriodoUtil.Rango rangoSemana = PeriodoUtil.semanaDe(semana != null ? semana : LocalDate.now());
        PeriodoUtil.Rango rangoQuincena = PeriodoUtil.quincenaDe(quincena != null ? quincena : LocalDate.now());

        List<RegistroHoras> registrosSemana = registroHorasService.listarPorProfesorEnRango(
                profesorId, rangoSemana.inicio(), rangoSemana.fin());
        List<RegistroHoras> registrosQuincena = registroHorasService.listarPorProfesorEnRango(
                profesorId, rangoQuincena.inicio(), rangoQuincena.fin());
        List<PagoProfesor> pagos = pagoProfesorService.listarPorProfesor(profesorId);
        Map<Long, RegistroHoras> registrosHoyPorHorario = registroHorasService.buscarDeHoyPorHorarios(
                horarios.stream().map(Horario::getId).toList());

        BigDecimal horasSemana = sumarHoras(registrosSemana);
        BigDecimal horasQuincena = sumarHoras(registrosQuincena);
        BigDecimal tarifa = profesor.getTarifaHora() != null ? profesor.getTarifaHora() : BigDecimal.ZERO;
        BigDecimal montoEsperadoSemana = horasSemana.multiply(tarifa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoEsperadoQuincena = horasQuincena.multiply(tarifa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoPagadoSemana = sumarPagosDelPeriodo(pagos, "SEMANAL", rangoSemana);
        BigDecimal montoPagadoQuincena = sumarPagosDelPeriodo(pagos, "QUINCENAL", rangoQuincena);

        model.addAttribute("profesor", profesor);
        model.addAttribute("horarios", horarios);
        model.addAttribute("registrosHoyPorHorario", registrosHoyPorHorario);
        model.addAttribute("rangoSemana", rangoSemana);
        model.addAttribute("rangoQuincena", rangoQuincena);
        model.addAttribute("registrosSemana", registrosSemana);
        model.addAttribute("registrosQuincena", registrosQuincena);
        model.addAttribute("horasSemana", horasSemana);
        model.addAttribute("horasQuincena", horasQuincena);
        model.addAttribute("montoEsperadoSemana", montoEsperadoSemana);
        model.addAttribute("montoEsperadoQuincena", montoEsperadoQuincena);
        model.addAttribute("montoPagadoSemana", montoPagadoSemana);
        model.addAttribute("montoPagadoQuincena", montoPagadoQuincena);
        model.addAttribute("pendienteSemana", montoEsperadoSemana.subtract(montoPagadoSemana));
        model.addAttribute("pendienteQuincena", montoEsperadoQuincena.subtract(montoPagadoQuincena));
        model.addAttribute("pagos", pagos);

        model.addAttribute("semanaAnteriorAnchor", PeriodoUtil.semanaAnterior(rangoSemana).inicio());
        model.addAttribute("semanaSiguienteAnchor", PeriodoUtil.semanaSiguiente(rangoSemana).inicio());
        model.addAttribute("quincenaAnteriorAnchor", PeriodoUtil.quincenaAnterior(rangoQuincena).inicio());
        model.addAttribute("quincenaSiguienteAnchor", PeriodoUtil.quincenaSiguiente(rangoQuincena).inicio());
        model.addAttribute("semanaAnchor", rangoSemana.inicio());
        model.addAttribute("quincenaAnchor", rangoQuincena.inicio());
        return "horas-docentes/lista";
    }

    @GetMapping("/registrar/{horarioId}")
    public String nuevoRegistro(@PathVariable Long horarioId,
                                @RequestParam(required = false) LocalDate fecha,
                                Model model) {
        Horario horario = horarioService.buscarPorId(horarioId);
        LocalDate fechaSel = fecha != null ? fecha : LocalDate.now();
        RegistroHoras registroDeHoy = registroHorasService.buscarDeHoyPorHorarios(List.of(horarioId)).get(horarioId);
        boolean esHoyConDatos = registroDeHoy != null && fechaSel.isEqual(LocalDate.now());

        model.addAttribute("horario", horario);
        model.addAttribute("fecha", fechaSel);
        model.addAttribute("registroDeHoy", esHoyConDatos ? registroDeHoy : null);
        model.addAttribute("horaInicioSel", esHoyConDatos && registroDeHoy.getHoraInicio() != null
                ? registroDeHoy.getHoraInicio() : horario.getHoraInicio());
        model.addAttribute("horaFinSel", esHoyConDatos && registroDeHoy.getHoraFin() != null
                ? registroDeHoy.getHoraFin() : horario.getHoraFin());
        model.addAttribute("observacionesSel", esHoyConDatos ? registroDeHoy.getObservaciones() : null);
        model.addAttribute("diaSemanaJs", horario.getDiaSemana().ordinal() + 1);
        if (horario.getHoraInicio() != null && horario.getHoraFin() != null) {
            model.addAttribute("horaInicioMin", horario.getHoraInicio().minusMinutes(30));
            model.addAttribute("horaFinMax", horario.getHoraFin().plusMinutes(30));
        }
        return "horas-docentes/registrar";
    }

    @PostMapping("/llegada")
    public String marcarLlegada(@RequestParam Long horarioId, Authentication auth, RedirectAttributes ra) {
        String username = (auth != null) ? auth.getName() : null;
        RegistroHoras registro = registroHorasService.marcarLlegada(horarioId, LocalDate.now(), username);
        ra.addFlashAttribute("mensajeExito", "Llegada registrada.");
        return "redirect:/horas-docentes?profesorId=" + registro.getProfesor().getId();
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam Long horarioId,
                            @RequestParam LocalDate fecha,
                            @RequestParam LocalTime horaInicio,
                            @RequestParam LocalTime horaFin,
                            @RequestParam(required = false) String observaciones,
                            Authentication auth, RedirectAttributes ra) {
        String username = (auth != null) ? auth.getName() : null;
        RegistroHoras registro = registroHorasService.registrarHoras(horarioId, fecha, horaInicio, horaFin, observaciones, username);
        ra.addFlashAttribute("mensajeExito", "Horas registradas correctamente.");
        return "redirect:/horas-docentes?profesorId=" + registro.getProfesor().getId();
    }

    @PostMapping("/pagos/registrar")
    public String registrarPago(@RequestParam Long profesorId,
                                @RequestParam String tipoPeriodo,
                                @RequestParam LocalDate periodoInicio,
                                @RequestParam LocalDate periodoFin,
                                @RequestParam BigDecimal horasPagadas,
                                @RequestParam BigDecimal monto,
                                @RequestParam(required = false) LocalDate fechaPago,
                                @RequestParam(required = false) String metodo,
                                @RequestParam(required = false) String observaciones,
                                Authentication auth, RedirectAttributes ra) {
        String username = (auth != null) ? auth.getName() : null;
        pagoProfesorService.registrar(profesorId, tipoPeriodo, periodoInicio, periodoFin, horasPagadas, monto,
                fechaPago, metodo, observaciones, username);
        ra.addFlashAttribute("mensajeExito", "Pago registrado correctamente.");
        return "redirect:/horas-docentes?profesorId=" + profesorId;
    }

    private BigDecimal sumarHoras(List<RegistroHoras> registros) {
        return registros.stream()
                .map(r -> r.getHoras() != null ? r.getHoras() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarPagosDelPeriodo(List<PagoProfesor> pagos, String tipo, PeriodoUtil.Rango rango) {
        return pagos.stream()
                .filter(p -> tipo.equals(p.getTipoPeriodo())
                        && p.getPeriodoInicio().isEqual(rango.inicio())
                        && p.getPeriodoFin().isEqual(rango.fin()))
                .map(PagoProfesor::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
