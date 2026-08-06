package com.unaj.project.controller;

import com.unaj.project.model.Alumno;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Turno;
import com.unaj.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.util.List;

@Controller
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;
    private final AlumnoService alumnoService;
    private final CicloService cicloService;
    private final CursoService cursoService;
    private final PagoService pagoService;
    private final ConfiguracionService configuracionService;
    private final PdfGeneradorService pdfGeneradorService;

    public MatriculaController(MatriculaService matriculaService,
                               AlumnoService alumnoService,
                               CicloService cicloService,
                               CursoService cursoService,
                               PagoService pagoService,
                               ConfiguracionService configuracionService,
                               PdfGeneradorService pdfGeneradorService) {
        this.matriculaService = matriculaService;
        this.alumnoService = alumnoService;
        this.cicloService = cicloService;
        this.cursoService = cursoService;
        this.pagoService = pagoService;
        this.configuracionService = configuracionService;
        this.pdfGeneradorService = pdfGeneradorService;
    }
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        Page<Matricula> pagina = matriculaService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("matriculas", pagina.getContent());
        model.addAttribute("q", q);
        return "matriculas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("alumnos", alumnoService.listarTodos());
        model.addAttribute("cursos", cursoService.listarTodos());
        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("cicloActivo", cicloService.obtenerActivo());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("areas", Areas.TODAS);
        model.addAttribute("configuracion", configuracionService.obtener());
        return "matriculas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long estudianteId,
                          @RequestParam Long cicloId,
                          @RequestParam Turno turno,
                          @RequestParam String area,
                          @RequestParam(required = false) String conceptoMatricula,
                          @RequestParam(required = false) java.math.BigDecimal montoMatricula,
                          @RequestParam(required = false) String conceptoPension,
                          @RequestParam(required = false) java.math.BigDecimal montoPension,
                          Model model,
                          RedirectAttributes ra) {
        try {
            matriculaService.matricular(
                    estudianteId, cicloId, turno, area,
                    conceptoMatricula, montoMatricula, conceptoPension, montoPension);
            ra.addFlashAttribute("mensajeExito", "Matrícula guardada correctamente.");
            return "redirect:/matriculas";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("alumnos", alumnoService.listarTodos());
            model.addAttribute("cursos", cursoService.listarTodos());
            model.addAttribute("ciclos", cicloService.listarTodos());
            model.addAttribute("cicloActivo", cicloService.obtenerActivo());
            model.addAttribute("turnos", Turno.values());
            model.addAttribute("areas", Areas.TODAS);
            model.addAttribute("configuracion", configuracionService.obtener());
            return "matriculas/formulario";
        }
    }
    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        matriculaService.anular(id);
        ra.addFlashAttribute("mensajeExito", "Matrícula anulada correctamente.");
        return "redirect:/matriculas";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        matriculaService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Matrícula eliminada correctamente.");
        return "redirect:/matriculas";
    }

    @GetMapping("/ficha/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return "redirect:/matriculas";
        }
        model.addAttribute("matricula", matricula);
        model.addAttribute("pagos", pagoService.listarPorMatricula(id));
        return "matriculas/ficha";
    }

    @GetMapping("/ficha/{id}/pdf")
    public ResponseEntity<byte[]> fichaPdf(@PathVariable Long id) {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return ResponseEntity.notFound().build();
        }

        Context context = new Context();
        context.setVariable("matricula", matricula);
        context.setVariable("pagos", pagoService.listarPorMatricula(id));

        byte[] pdfBytes = pdfGeneradorService.renderizar("matriculas/ficha-pdf", context);
        String filename = "constancia_matricula_" + matricula.getEstudiante().getId() + "_" + matricula.getId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }
}