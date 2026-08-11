package com.unaj.project.controller;

import com.unaj.project.dto.LogroIngresoForm;
import com.unaj.project.model.LogroIngreso;
import com.unaj.project.service.LogroIngresoService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/logros-ingreso")
public class LogroIngresoController {

    private final LogroIngresoService logroIngresoService;

    public LogroIngresoController(LogroIngresoService logroIngresoService) {
        this.logroIngresoService = logroIngresoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("logros", logroIngresoService.listarTodos());
        return "logros-ingreso/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("logroForm", new LogroIngresoForm());
        return "logros-ingreso/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("logroForm", logroIngresoService.buscarFormPorId(id));
        return "logros-ingreso/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("logroForm") LogroIngresoForm logroForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "logros-ingreso/formulario";
        }
        logroIngresoService.guardar(logroForm);
        ra.addFlashAttribute("mensajeExito", "Resultado de ingreso guardado correctamente.");
        return "redirect:/logros-ingreso";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        logroIngresoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Resultado de ingreso eliminado correctamente.");
        return "redirect:/logros-ingreso";
    }

    @GetMapping("/{id}/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        LogroIngreso l = logroIngresoService.buscarPorId(id);
        if (!l.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (l.getFotoContentType() != null)
                ? MediaType.parseMediaType(l.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(l.getFoto());
    }
}
