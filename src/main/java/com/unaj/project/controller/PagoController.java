package com.unaj.project.controller;

import com.unaj.project.model.Pago;
import com.unaj.project.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<Pago> pagos = pagoService.listarTodos();
        model.addAttribute("pagos", pagos);
        model.addAttribute("cobrado", pagoService.totalPorEstado(pagos, "PAGADO"));
        model.addAttribute("porCobrar", pagoService.totalPorEstado(pagos, "PENDIENTE"));
        model.addAttribute("vencido", pagoService.totalPorEstado(pagos, "VENCIDO"));
        return "pagos/lista";
    }

    @PostMapping("/registrar/{id}")
    public String registrar(@PathVariable Long id,
                            @RequestParam(defaultValue = "EFECTIVO") String metodo) {
        pagoService.registrarPago(id, metodo);
        return "redirect:/pagos";
    }
}