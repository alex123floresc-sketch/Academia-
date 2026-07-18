package com.unaj.project.controller;

import com.unaj.project.model.Alumno;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Turno;
import com.unaj.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;
    private final AlumnoService alumnoService;
    private final CicloService cicloService;
    private final CursoService cursoService;
    private final PagoService pagoService;
    private final TemplateEngine templateEngine;

    public MatriculaController(MatriculaService matriculaService,
                               AlumnoService alumnoService,
                               CicloService cicloService,
                               CursoService cursoService,
                               PagoService pagoService,
                               TemplateEngine templateEngine) {
        this.matriculaService = matriculaService;
        this.alumnoService = alumnoService;
        this.cicloService = cicloService;
        this.cursoService = cursoService;
        this.pagoService = pagoService;
        this.templateEngine = templateEngine;
    }
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("matriculas", matriculaService.listarTodos());
        return "matriculas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("alumnos", alumnoService.listarTodos());
        model.addAttribute("cursos", cursoService.listarTodos());
        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("cicloActivo", cicloService.obtenerActivo());
        model.addAttribute("turnos", Turno.values());
        return "matriculas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long estudianteId,
                          @RequestParam Long cicloId,
                          @RequestParam Turno turno,
                          @RequestParam(required = false) List<Long> cursoIds,
                          @RequestParam(required = false) String conceptoMatricula,
                          @RequestParam(required = false) java.math.BigDecimal montoMatricula,
                          @RequestParam(required = false) String conceptoPension,
                          @RequestParam(required = false) java.math.BigDecimal montoPension,
                          Model model) {
        try {
            Matricula matricula = matriculaService.matricular(
                    estudianteId, cicloId, turno, cursoIds,
                    conceptoMatricula, montoMatricula, conceptoPension, montoPension);
            return "redirect:/matriculas/ficha/" + matricula.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("alumnos", alumnoService.listarTodos());
            model.addAttribute("cursos", cursoService.listarTodos());
            model.addAttribute("ciclos", cicloService.listarTodos());
            model.addAttribute("cicloActivo", cicloService.obtenerActivo());
            model.addAttribute("turnos", Turno.values());
            return "matriculas/formulario";
        }
    }
    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id) {
        matriculaService.anular(id);
        return "redirect:/matriculas";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        matriculaService.eliminar(id);
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
    public ResponseEntity<byte[]> fichaPdf(@PathVariable Long id) throws Exception {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return ResponseEntity.notFound().build();
        }

        Context context = new Context();
        context.setVariable("matricula", matricula);
        context.setVariable("pagos", pagoService.listarPorMatricula(id));

        String html = templateEngine.process("matriculas/ficha-pdf", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        byte[] pdfBytes = outputStream.toByteArray();
        String filename = "constancia_matricula_" + matricula.getEstudiante().getId() + "_" + matricula.getId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }
}