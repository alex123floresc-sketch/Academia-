package com.unaj.project.controller;

import com.unaj.project.dto.ConfiguracionForm;
import com.unaj.project.model.Configuracion;
import com.unaj.project.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public String ver(Model model) {
        Configuracion configuracion = configuracionService.obtener();
        model.addAttribute("configuracion", configuracion);
        model.addAttribute("configuracionForm", aForm(configuracion));
        return "configuracion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("configuracionForm") ConfiguracionForm form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("configuracion", configuracionService.obtener());
            return "configuracion/formulario";
        }
        configuracionService.actualizar(form);
        ra.addFlashAttribute("mensajeExito", "Configuración actualizada correctamente.");
        return "redirect:/configuracion";
    }

    @GetMapping("/logo")
    @ResponseBody
    public ResponseEntity<byte[]> logo() {
        Configuracion configuracion = configuracionService.obtener();
        if (!configuracion.isLogoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (configuracion.getLogoContentType() != null)
                ? MediaType.parseMediaType(configuracion.getLogoContentType())
                : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().contentType(tipo).body(configuracion.getLogo());
    }

    @GetMapping("/favicon")
    @ResponseBody
    public ResponseEntity<byte[]> favicon() {
        Configuracion configuracion = configuracionService.obtener();
        if (!configuracion.isFaviconPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (configuracion.getFaviconContentType() != null)
                ? MediaType.parseMediaType(configuracion.getFaviconContentType())
                : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().contentType(tipo).body(configuracion.getFavicon());
    }

    @GetMapping("/fondo-login")
    @ResponseBody
    public ResponseEntity<byte[]> fondoLogin() {
        Configuracion configuracion = configuracionService.obtener();
        if (!configuracion.isFondoLoginPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (configuracion.getFondoLoginContentType() != null)
                ? MediaType.parseMediaType(configuracion.getFondoLoginContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(configuracion.getFondoLogin());
    }

    private ConfiguracionForm aForm(Configuracion c) {
        ConfiguracionForm form = new ConfiguracionForm();
        form.setMontoMatricula(c.getMontoMatricula());
        form.setMontoPension(c.getMontoPension());
        form.setNumeroCuotasPension(c.getNumeroCuotasPension());
        form.setDiasEntreCuotas(c.getDiasEntreCuotas());
        form.setDiasGraciaVencimiento(c.getDiasGraciaVencimiento());
        form.setCupoPorTurno(c.getCupoPorTurno());
        form.setToleranciaMinutosHorasDocentes(c.getToleranciaMinutosHorasDocentes());
        form.setDiasAvisoVencimiento(c.getDiasAvisoVencimiento());
        form.setNombreAcademia(c.getNombreAcademia());
        form.setTelefonoContacto(c.getTelefonoContacto());
        form.setDireccion(c.getDireccion());
        form.setColorAcento(c.getColorAcento());
        form.setSimboloMoneda(c.getSimboloMoneda());
        form.setEslogan(c.getEslogan());
        form.setCorreoContacto(c.getCorreoContacto());
        form.setVision(c.getVision());
        form.setMision(c.getMision());
        form.setSobreNosotros(c.getSobreNosotros());
        form.setMostrarVisionMision(c.isMostrarVisionMision());
        form.setMostrarDocentes(c.isMostrarDocentes());
        form.setMostrarCursos(c.isMostrarCursos());
        form.setMostrarSedes(c.isMostrarSedes());
        form.setWhatsappNumero(c.getWhatsappNumero());
        form.setFacebookUrl(c.getFacebookUrl());
        form.setInstagramUrl(c.getInstagramUrl());
        form.setTiktokUrl(c.getTiktokUrl());
        form.setAnioFundacion(c.getAnioFundacion());
        form.setDescripcionIngenierias(c.getDescripcionIngenierias());
        form.setDescripcionBiomedicas(c.getDescripcionBiomedicas());
        form.setDescripcionSociales(c.getDescripcionSociales());
        form.setMostrarProcesoAdmision(c.isMostrarProcesoAdmision());
        form.setMostrarFaq(c.isMostrarFaq());
        form.setMostrarTestimonios(c.isMostrarTestimonios());
        form.setMostrarLogros(c.isMostrarLogros());
        form.setMostrarGaleria(c.isMostrarGaleria());
        form.setMostrarCalendario(c.isMostrarCalendario());
        form.setMostrarFormularioContacto(c.isMostrarFormularioContacto());
        return form;
    }
}
