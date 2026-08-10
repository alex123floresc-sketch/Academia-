package com.unaj.project.controller;

import com.unaj.project.model.Nivel;
import com.unaj.project.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumenController {

    private final DashboardService dashboardService;

    public ResumenController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public String resumen(@RequestParam(required = false) Nivel nivel, Model model) {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;
        model.addAllAttributes(dashboardService.resumenInicio(nivelSel));
        return "resumen";
    }
}
