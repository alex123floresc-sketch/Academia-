package com.unaj.project.controller;

import com.unaj.project.dto.SedeForm;
import com.unaj.project.model.Sede;
import com.unaj.project.service.SedeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sedes")
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sedes", sedeService.listarTodas());
        return "sedes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("sedeForm", new SedeForm());
        return "sedes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("sedeForm", sedeService.buscarFormPorId(id));
        return "sedes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("sedeForm") SedeForm sedeForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "sedes/formulario";
        }
        sedeService.guardar(sedeForm);
        ra.addFlashAttribute("mensajeExito", "Sede guardada correctamente.");
        return "redirect:/sedes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        sedeService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Sede eliminada correctamente.");
        return "redirect:/sedes";
    }

    @GetMapping("/{id}/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        Sede sede = sedeService.buscarPorId(id);
        if (!sede.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (sede.getFotoContentType() != null)
                ? MediaType.parseMediaType(sede.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(sede.getFoto());
    }
}
