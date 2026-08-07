package com.unaj.project.controller;

import com.unaj.project.model.Matricula;
import com.unaj.project.service.MatriculaService;
import com.unaj.project.service.PagoService;
import com.unaj.project.service.PdfGeneradorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

@Controller
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final PdfGeneradorService pdfGeneradorService;

    public MatriculaController(MatriculaService matriculaService,
                               PagoService pagoService,
                               PdfGeneradorService pdfGeneradorService) {
        this.matriculaService = matriculaService;
        this.pagoService = pagoService;
        this.pdfGeneradorService = pdfGeneradorService;
    }

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        Matricula matricula = matriculaService.buscarPorId(id);
        Long alumnoId = matricula != null ? matricula.getEstudiante().getId() : null;
        matriculaService.anular(id);
        ra.addFlashAttribute("mensajeExito", "Matrícula anulada correctamente.");
        return alumnoId != null ? "redirect:/alumnos/" + alumnoId + "/expediente" : "redirect:/alumnos";
    }

    @GetMapping("/ficha/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return "redirect:/alumnos";
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
