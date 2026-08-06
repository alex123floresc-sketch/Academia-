package com.unaj.project.controller;

import com.unaj.project.service.ConfiguracionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("configuracion", configuracionService.obtener());
        return "configuracion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam BigDecimal montoMatricula,
                          @RequestParam BigDecimal montoPension,
                          RedirectAttributes ra) {
        configuracionService.actualizar(montoMatricula, montoPension);
        ra.addFlashAttribute("mensajeExito", "Precios actualizados correctamente.");
        return "redirect:/configuracion";
    }
}
