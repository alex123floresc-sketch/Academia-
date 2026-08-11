package com.unaj.project.controller;

import com.unaj.project.model.Matricula;
import com.unaj.project.model.Usuario;
import com.unaj.project.service.MatriculaService;
import com.unaj.project.service.PagoService;
import com.unaj.project.service.PdfGeneradorService;
import com.unaj.project.service.PerfilService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.util.Base64;

@Controller
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final PdfGeneradorService pdfGeneradorService;
    private final PerfilService perfilService;

    public MatriculaController(MatriculaService matriculaService,
                               PagoService pagoService,
                               PdfGeneradorService pdfGeneradorService,
                               PerfilService perfilService) {
        this.matriculaService = matriculaService;
        this.pagoService = pagoService;
        this.pdfGeneradorService = pdfGeneradorService;
        this.perfilService = perfilService;
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
    public String ficha(@PathVariable Long id, Model model, Authentication auth) {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return "redirect:/alumnos";
        }
        model.addAttribute("matricula", matricula);
        model.addAttribute("pagos", pagoService.listarPorMatricula(id));
        model.addAttribute("atendidoPor", perfilService.obtenerPropio(auth.getName()));
        return "matriculas/ficha";
    }

    @GetMapping("/ficha/{id}/pdf")
    public ResponseEntity<byte[]> fichaPdf(@PathVariable Long id, Authentication auth) {
        Matricula matricula = matriculaService.buscarFichaPorId(id);
        if (matricula == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario atendidoPor = perfilService.obtenerPropio(auth.getName());
        String firmaDataUri = null;
        if (atendidoPor.isFirmaPresente()) {
            String tipo = (atendidoPor.getFirmaContentType() != null) ? atendidoPor.getFirmaContentType() : "image/png";
            firmaDataUri = "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(atendidoPor.getFirma());
        }

        Context context = new Context();
        context.setVariable("matricula", matricula);
        context.setVariable("pagos", pagoService.listarPorMatricula(id));
        context.setVariable("atendidoPorNombre", atendidoPor.getNombre());
        context.setVariable("firmaDataUri", firmaDataUri);

        byte[] pdfBytes = pdfGeneradorService.renderizar("matriculas/ficha-pdf", context);
        String filename = "constancia_matricula_" + matricula.getEstudiante().getId() + "_" + matricula.getId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }
}
