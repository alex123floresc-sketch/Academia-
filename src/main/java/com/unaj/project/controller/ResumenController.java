package com.unaj.project.controller;

import com.unaj.project.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResumenController {

    private final DashboardService dashboardService;

    public ResumenController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public String resumen(Model model) {
        model.addAllAttributes(dashboardService.resumenInicio());
        return "resumen";
    }
}
