package com.unaj.project.controller;

import com.unaj.project.dto.FotoGaleriaForm;
import com.unaj.project.model.FotoGaleria;
import com.unaj.project.service.FotoGaleriaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/galeria")
public class FotoGaleriaController {

    private final FotoGaleriaService fotoGaleriaService;

    public FotoGaleriaController(FotoGaleriaService fotoGaleriaService) {
        this.fotoGaleriaService = fotoGaleriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fotos", fotoGaleriaService.listarTodas());
        return "galeria/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("fotoForm", new FotoGaleriaForm());
        return "galeria/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fotoForm", fotoGaleriaService.buscarFormPorId(id));
        return "galeria/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("fotoForm") FotoGaleriaForm fotoForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "galeria/formulario";
        }
        fotoGaleriaService.guardar(fotoForm);
        ra.addFlashAttribute("mensajeExito", "Foto guardada correctamente.");
        return "redirect:/galeria";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        fotoGaleriaService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Foto eliminada correctamente.");
        return "redirect:/galeria";
    }

    @GetMapping("/{id}/imagen")
    @ResponseBody
    public ResponseEntity<byte[]> imagen(@PathVariable Long id) {
        FotoGaleria f = fotoGaleriaService.buscarPorId(id);
        if (!f.isImagenPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (f.getImagenContentType() != null)
                ? MediaType.parseMediaType(f.getImagenContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(f.getImagen());
    }
}
