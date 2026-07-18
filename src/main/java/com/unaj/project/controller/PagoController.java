package com.unaj.project.controller;

import com.unaj.project.model.Pago;
import com.unaj.project.service.PagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15) Pageable pageable,
                         Model model) {
        Page<Pago> pagina = pagoService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("pagos", pagina.getContent());
        model.addAttribute("q", q);

        List<Pago> todos = pagoService.listarTodos();
        model.addAttribute("cobrado", pagoService.totalPorEstado(todos, "PAGADO"));
        model.addAttribute("porCobrar", pagoService.totalPorEstado(todos, "PENDIENTE"));
        model.addAttribute("vencido", pagoService.totalPorEstado(todos, "VENCIDO"));
        return "pagos/lista";
    }

    @PostMapping("/registrar/{id}")
    public String registrar(@PathVariable Long id,
                            @RequestParam(defaultValue = "EFECTIVO") String metodo,
                            RedirectAttributes ra) {
        pagoService.registrarPago(id, metodo);
        ra.addFlashAttribute("mensajeExito", "Pago registrado correctamente.");
        return "redirect:/pagos";
    }
}