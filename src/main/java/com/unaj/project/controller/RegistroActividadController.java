package com.unaj.project.controller;

import com.unaj.project.model.RegistroActividad;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/actividad")
public class RegistroActividadController {

    private final RegistroActividadService registroActividadService;

    public RegistroActividadController(RegistroActividadService registroActividadService) {
        this.registroActividadService = registroActividadService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String username,
                         @RequestParam(required = false) String modulo,
                         @RequestParam(required = false) TipoAccion accion,
                         @PageableDefault(size = 20) Pageable pageable,
                         Model model) {
        Page<RegistroActividad> pagina = registroActividadService.buscarPagina(username, modulo, accion, q, pageable);

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime inicioSemana = LocalDate.now().minusDays(6).atStartOfDay();

        model.addAttribute("pagina", pagina);
        model.addAttribute("registros", pagina.getContent());
        model.addAttribute("q", q);
        model.addAttribute("username", username);
        model.addAttribute("modulo", modulo);
        model.addAttribute("accion", accion);
        model.addAttribute("modulos", registroActividadService.modulos());
        model.addAttribute("acciones", TipoAccion.values());
        model.addAttribute("totalHoy", registroActividadService.contarDesde(inicioHoy));
        model.addAttribute("totalSemana", registroActividadService.contarDesde(inicioSemana));
        return "actividad/lista";
    }
}
