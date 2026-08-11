package com.unaj.project.controller;

import com.unaj.project.dto.TestimonioForm;
import com.unaj.project.model.Testimonio;
import com.unaj.project.service.TestimonioService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/testimonios")
public class TestimonioController {

    private final TestimonioService testimonioService;

    public TestimonioController(TestimonioService testimonioService) {
        this.testimonioService = testimonioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("testimonios", testimonioService.listarTodos());
        return "testimonios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("testimonioForm", new TestimonioForm());
        return "testimonios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("testimonioForm", testimonioService.buscarFormPorId(id));
        return "testimonios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("testimonioForm") TestimonioForm testimonioForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "testimonios/formulario";
        }
        testimonioService.guardar(testimonioForm);
        ra.addFlashAttribute("mensajeExito", "Testimonio guardado correctamente.");
        return "redirect:/testimonios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        testimonioService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Testimonio eliminado correctamente.");
        return "redirect:/testimonios";
    }

    @GetMapping("/{id}/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        Testimonio t = testimonioService.buscarPorId(id);
        if (!t.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (t.getFotoContentType() != null)
                ? MediaType.parseMediaType(t.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(t.getFoto());
    }
}
