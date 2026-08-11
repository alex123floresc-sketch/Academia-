package com.unaj.project.controller;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.PagoProfesor;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.RegistroHoras;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.service.PagoProfesorService;
import com.unaj.project.service.ProfesorService;
import com.unaj.project.service.RegistroHorasService;
import com.unaj.project.util.PeriodoUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/profesores")
public class ProfesorController {

    private final ProfesorService profesorService;
    private final CursoRepository cursoRepository;
    private final HorarioRepository horarioRepository;
    private final RegistroHorasService registroHorasService;
    private final PagoProfesorService pagoProfesorService;

    public ProfesorController(ProfesorService profesorService,
                              CursoRepository cursoRepository,
                              HorarioRepository horarioRepository,
                              RegistroHorasService registroHorasService,
                              PagoProfesorService pagoProfesorService) {
        this.profesorService = profesorService;
        this.cursoRepository = cursoRepository;
        this.horarioRepository = horarioRepository;
        this.registroHorasService = registroHorasService;
        this.pagoProfesorService = pagoProfesorService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        Page<Profesor> pagina = profesorService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("profesores", pagina.getContent());
        model.addAttribute("q", q);
        return "profesores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("profesorForm", new ProfesorForm());
        model.addAttribute("niveles", Nivel.values());
        return "profesores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("profesorForm") ProfesorForm profesorForm,
                          BindingResult result,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("niveles", Nivel.values());
            return "profesores/formulario";
        }
        profesorService.guardar(profesorForm);
        ra.addFlashAttribute("mensajeExito", "Profesor guardado correctamente.");
        return "redirect:/profesores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("profesorForm", profesorService.buscarFormPorId(id));
        model.addAttribute("tieneFoto", profesorService.buscarPorId(id).isFotoPresente());
        model.addAttribute("niveles", Nivel.values());
        return "profesores/formulario";
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        Profesor profesor = profesorService.buscarPorId(id);
        if (!profesor.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (profesor.getFotoContentType() != null)
                ? MediaType.parseMediaType(profesor.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(profesor.getFoto());
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Profesor profesor = profesorService.buscarPorId(id);
        List<Curso> cursos = cursoRepository.findByProfesorIdAndEliminadoFalse(id);
        List<Horario> horarios = horarioRepository.findByCursoProfesorId(id).stream()
                .sorted(Comparator.comparing(Horario::getDiaSemana)
                        .thenComparing(Horario::getTurno)
                        .thenComparing(Horario::getHoraInicio))
                .toList();

        PeriodoUtil.Rango rangoQuincena = PeriodoUtil.quincenaDe(LocalDate.now());
        List<RegistroHoras> registrosQuincena = registroHorasService.listarPorProfesorEnRango(
                id, rangoQuincena.inicio(), rangoQuincena.fin());
        BigDecimal horasQuincena = registrosQuincena.stream()
                .map(r -> r.getHoras() != null ? r.getHoras() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tarifa = profesor.getTarifaHora() != null ? profesor.getTarifaHora() : BigDecimal.ZERO;
        BigDecimal montoEsperadoQuincena = horasQuincena.multiply(tarifa).setScale(2, RoundingMode.HALF_UP);

        List<PagoProfesor> pagos = pagoProfesorService.listarPorProfesor(id);
        BigDecimal montoPagadoQuincena = pagos.stream()
                .filter(p -> "QUINCENAL".equals(p.getTipoPeriodo())
                        && p.getPeriodoInicio().isEqual(rangoQuincena.inicio())
                        && p.getPeriodoFin().isEqual(rangoQuincena.fin()))
                .map(PagoProfesor::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("profesor", profesor);
        model.addAttribute("tieneFoto", profesor.isFotoPresente());
        model.addAttribute("cursos", cursos);
        model.addAttribute("horarios", horarios);
        model.addAttribute("rangoQuincena", rangoQuincena);
        model.addAttribute("horasQuincena", horasQuincena);
        model.addAttribute("montoEsperadoQuincena", montoEsperadoQuincena);
        model.addAttribute("montoPagadoQuincena", montoPagadoQuincena);
        model.addAttribute("pendienteQuincena", montoEsperadoQuincena.subtract(montoPagadoQuincena));
        model.addAttribute("ultimosRegistros", registroHorasService.listarPorProfesor(id).stream().limit(5).toList());
        model.addAttribute("ultimosPagos", pagos.stream().limit(5).toList());
        return "profesores/detalle";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        profesorService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Profesor eliminado correctamente.");
        return "redirect:/profesores";
    }
}