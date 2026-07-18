package com.unaj.project.controller;

import com.unaj.project.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    private final DashboardService dashboardService;

    public InicioController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAllAttributes(dashboardService.resumenInicio());
        return "inicio";
    }
}