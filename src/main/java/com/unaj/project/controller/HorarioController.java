package com.unaj.project.controller;

import com.unaj.project.dto.FilaHorarioDTO;
import com.unaj.project.model.*;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.PdfGeneradorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/horarios")
public class HorarioController {

    private final HorarioService horarioService;
    private final CicloService cicloService;
    private final CursoService cursoService;
    private final HorarioRepository horarioRepository;
    private final PdfGeneradorService pdfGeneradorService;

    public HorarioController(HorarioService horarioService,
                             CicloService cicloService,
                             CursoService cursoService,
                             HorarioRepository horarioRepository,
                             PdfGeneradorService pdfGeneradorService) {
        this.horarioService = horarioService;
        this.cicloService = cicloService;
        this.cursoService = cursoService;
        this.horarioRepository = horarioRepository;
        this.pdfGeneradorService = pdfGeneradorService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long cicloId, Model model) {
        Ciclo cicloSel = (cicloId != null) ? cicloService.buscarPorId(cicloId) : cicloService.obtenerActivo();

        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("cicloSel", cicloSel);
        model.addAttribute("diaHoy", DiaSemana.desde(LocalDate.now().getDayOfWeek()));

        if (cicloSel != null) {
            model.addAttribute("grilla", horarioService.agruparParaGrilla(cicloSel.getId()));
        }
        return "horarios/lista";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long cicloId) {
        Ciclo ciclo = cicloService.buscarPorId(cicloId);
        Context context = new Context();
        context.setVariable("ciclo", ciclo);
        context.setVariable("turnos", Turno.values());
        context.setVariable("dias", DiaSemana.values());
        context.setVariable("grilla", horarioService.agruparParaGrilla(cicloId));

        byte[] pdfBytes = pdfGeneradorService.renderizar("horarios/horario-pdf", context);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "horario_" + ciclo.getNombre() + ".pdf");
        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam Long cicloId, @RequestParam DiaSemana dia, @RequestParam Long bloqueId,
                        Model model) {
        prepararForm(model, cicloId, dia, bloqueId);
        return "horarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long cicloId,
                          @RequestParam DiaSemana dia,
                          @RequestParam Long bloqueId,
                          @RequestParam(required = false) List<Long> cursoIds,
                          Model model,
                          RedirectAttributes ra) {
        try {
            horarioService.asignarCurso(bloqueId, dia, cursoIds);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararForm(model, cicloId, dia, bloqueId);
            return "horarios/formulario";
        }
        ra.addFlashAttribute("mensajeExito", "Curso agregado al horario correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/quitar-curso/{horarioId}")
    public String quitarCurso(@PathVariable Long horarioId, @RequestParam Long cicloId, RedirectAttributes ra) {
        horarioService.quitarCurso(horarioId);
        ra.addFlashAttribute("mensajeExito", "Curso quitado del horario.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/bloques/guardar")
    public String guardarBloque(@RequestParam Long cicloId,
                                @RequestParam Turno turno,
                                @RequestParam String horaInicio,
                                @RequestParam String horaFin,
                                @RequestParam(defaultValue = "CLASE") TipoBloque tipo,
                                RedirectAttributes ra) {
        horarioService.crearBloque(cicloId, turno, LocalTime.parse(horaInicio), LocalTime.parse(horaFin), tipo);
        ra.addFlashAttribute("mensajeExito", "Bloque horario agregado correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    @PostMapping("/bloques/eliminar/{bloqueId}")
    public String eliminarBloque(@PathVariable Long bloqueId, @RequestParam Long cicloId, RedirectAttributes ra) {
        horarioService.eliminarBloque(bloqueId);
        ra.addFlashAttribute("mensajeExito", "Bloque horario eliminado correctamente.");
        return "redirect:/horarios?cicloId=" + cicloId;
    }

    private void prepararForm(Model model, Long cicloId, DiaSemana dia, Long bloqueId) {
        BloqueHorario bloque = horarioService.buscarBloque(bloqueId);
        model.addAttribute("bloque", bloque);
        model.addAttribute("ciclo", cicloService.buscarPorId(cicloId));
        model.addAttribute("cicloId", cicloId);
        model.addAttribute("dia", dia);
        model.addAttribute("cursos", cursoService.listarTodos());

        List<Horario> cursosAgregados = horarioRepository.findByBloqueIdAndDiaSemana(bloqueId, dia);
        model.addAttribute("cursosAgregados", cursosAgregados);
        model.addAttribute("cursoIdsAgregados",
                cursosAgregados.stream().map(h -> h.getCurso().getId()).collect(Collectors.toList()));
    }
}
