package com.unaj.project.controller;

import com.unaj.project.model.Nivel;
import com.unaj.project.service.DashboardService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumenController {

    private final DashboardService dashboardService;
    private final RegistroActividadService registroActividadService;

    public ResumenController(DashboardService dashboardService, RegistroActividadService registroActividadService) {
        this.dashboardService = dashboardService;
        this.registroActividadService = registroActividadService;
    }

    @GetMapping("/resumen")
    public String resumen(@RequestParam(required = false) Nivel nivel, Model model) {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;
        model.addAllAttributes(dashboardService.resumenInicio(nivelSel));
        model.addAttribute("actividadReciente", registroActividadService.actividadReciente());
        return "resumen";
    }
}
