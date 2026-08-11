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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
    public String listar(@RequestParam(required = false) Long cicloId,
                         @RequestParam(required = false) Nivel nivel,
                         @RequestParam(required = false) String area,
                         Model model) {
        Ciclo cicloSel = (cicloId != null) ? cicloService.buscarPorId(cicloId) : cicloService.obtenerActivo();
        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("cicloSel", cicloSel);

        if (nivel == null) {
            model.addAttribute("resumenNiveles", horarioService.contarBloquesPorNivel(cicloSel != null ? cicloSel.getId() : null));
            return "horarios/niveles";
        }

        List<String> areasDelNivel = Areas.paraNivel(nivel);
        String areaSel = (area != null && areasDelNivel.contains(area)) ? area : null;
        if (areaSel == null) {
            model.addAttribute("nivel", nivel);
            model.addAttribute("resumenAreas", horarioService.contarBloquesPorArea(cicloSel != null ? cicloSel.getId() : null, nivel));
            return "horarios/areas";
        }

        model.addAttribute("turnos", Turno.values());
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("diaHoy", DiaSemana.desde(LocalDate.now().getDayOfWeek()));
        model.addAttribute("nivel", nivel);
        model.addAttribute("area", areaSel);

        if (cicloSel != null) {
            Map<Turno, List<FilaHorarioDTO>> grilla = horarioService.agruparParaGrilla(cicloSel.getId(), nivel, areaSel);
            model.addAttribute("grilla", grilla);
            long totalBloques = grilla.values().stream().mapToLong(List::size).sum();
            long totalAsignaciones = grilla.values().stream()
                    .flatMap(List::stream)
                    .flatMap(f -> f.porDia().values().stream())
                    .mapToLong(List::size).sum();
            model.addAttribute("totalBloques", totalBloques);
            model.addAttribute("totalAsignaciones", totalAsignaciones);
        }
        return "horarios/lista";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long cicloId, @RequestParam Nivel nivel, @RequestParam String area) {
        Ciclo ciclo = cicloService.buscarPorId(cicloId);
        String titulo = "Horario de clases · " + ciclo.getNombre() + " · " + nivel.getEtiqueta() + " · " + area;
        Context context = new Context();
        context.setVariable("titulo", titulo);
        context.setVariable("turnos", Turno.values());
        context.setVariable("dias", DiaSemana.values());
        context.setVariable("grilla", horarioService.agruparParaGrilla(cicloId, nivel, area));

        byte[] pdfBytes = pdfGeneradorService.renderizar("horarios/horario-pdf", context);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "horario_" + ciclo.getNombre() + "_" + nivel.name() + "_" + area + ".pdf");
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
        BloqueHorario bloque = horarioService.buscarBloque(bloqueId);
        try {
            horarioService.asignarCurso(bloqueId, dia, cursoIds);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararForm(model, cicloId, dia, bloqueId);
            return "horarios/formulario";
        }
        ra.addFlashAttribute("mensajeExito", "Curso agregado al horario correctamente.");
        return redirectAGrilla(cicloId, bloque.getNivel(), bloque.getArea());
    }

    @PostMapping("/quitar-curso/{horarioId}")
    public String quitarCurso(@PathVariable Long horarioId, @RequestParam Long cicloId,
                              @RequestParam Nivel nivel, @RequestParam String area, RedirectAttributes ra) {
        horarioService.quitarCurso(horarioId);
        ra.addFlashAttribute("mensajeExito", "Curso quitado del horario.");
        return redirectAGrilla(cicloId, nivel, area);
    }

    @PostMapping("/bloques/guardar")
    public String guardarBloque(@RequestParam Long cicloId,
                                @RequestParam Nivel nivel,
                                @RequestParam Turno turno,
                                @RequestParam String horaInicio,
                                @RequestParam String horaFin,
                                @RequestParam(defaultValue = "CLASE") TipoBloque tipo,
                                @RequestParam String area,
                                RedirectAttributes ra) {
        horarioService.crearBloque(cicloId, nivel, turno, LocalTime.parse(horaInicio), LocalTime.parse(horaFin), tipo, area);
        ra.addFlashAttribute("mensajeExito", "Bloque horario agregado correctamente.");
        return redirectAGrilla(cicloId, nivel, area);
    }

    @PostMapping("/bloques/eliminar/{bloqueId}")
    public String eliminarBloque(@PathVariable Long bloqueId, @RequestParam Long cicloId,
                                 @RequestParam Nivel nivel, @RequestParam String area, RedirectAttributes ra) {
        horarioService.eliminarBloque(bloqueId);
        ra.addFlashAttribute("mensajeExito", "Bloque horario eliminado correctamente.");
        return redirectAGrilla(cicloId, nivel, area);
    }

    private String redirectAGrilla(Long cicloId, Nivel nivel, String area) {
        return "redirect:/horarios?cicloId=" + cicloId
                + "&nivel=" + nivel.name()
                + "&area=" + URLEncoder.encode(area, StandardCharsets.UTF_8);
    }

    private void prepararForm(Model model, Long cicloId, DiaSemana dia, Long bloqueId) {
        BloqueHorario bloque = horarioService.buscarBloque(bloqueId);
        model.addAttribute("bloque", bloque);
        model.addAttribute("ciclo", cicloService.buscarPorId(cicloId));
        model.addAttribute("cicloId", cicloId);
        model.addAttribute("dia", dia);
        model.addAttribute("cursos", cursoService.listarPorNivelYArea(bloque.getNivel(), bloque.getArea()));

        List<Horario> cursosAgregados = horarioRepository.findByBloqueIdAndDiaSemana(bloqueId, dia);
        model.addAttribute("cursosAgregados", cursosAgregados);
        model.addAttribute("cursoIdsAgregados",
                cursosAgregados.stream().map(h -> h.getCurso().getId()).collect(Collectors.toList()));
    }
}
