package com.unaj.project.controller;

import com.unaj.project.model.Configuracion;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.ConfiguracionService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pone la identidad de la academia (nombre, logo, color de acento) y datos livianos de la sesión
 * actual (si tiene foto de perfil) a disposición de toda vista renderizada por un @Controller,
 * sin que cada controlador tenga que declararlos explícitamente. Los PDF generados vía
 * PdfGeneradorServiceImpl no pasan por aquí (usan un Context de Thymeleaf aparte) y reciben las
 * mismas variables de marca desde ese servicio.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final ConfiguracionService configuracionService;
    private final UsuarioRepository usuarioRepository;

    public GlobalModelAttributes(ConfiguracionService configuracionService, UsuarioRepository usuarioRepository) {
        this.configuracionService = configuracionService;
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute
    public void inyectarMarca(Model model) {
        Configuracion configuracion = configuracionService.obtener();
        model.addAttribute("marcaNombre", configuracion.getNombreAcademia());
        model.addAttribute("marcaTieneLogo", configuracion.isLogoPresente());
        model.addAttribute("marcaColor", configuracion.getColorAcento());
        model.addAttribute("marcaMoneda", configuracion.getSimboloMoneda());
        model.addAttribute("marcaEslogan", configuracion.getEslogan());
        model.addAttribute("marcaTieneFavicon", configuracion.isFaviconPresente());
        model.addAttribute("marcaTieneFondoLogin", configuracion.isFondoLoginPresente());
    }

    @ModelAttribute
    public void inyectarUsuarioActual(Model model, Authentication auth) {
        Usuario usuario = (auth != null) ? usuarioRepository.findByUsername(auth.getName()) : null;
        model.addAttribute("usuarioActualTieneFoto", usuario != null && usuario.isFotoPresente());
    }
}
