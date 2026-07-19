package com.unaj.project.controller;

import com.unaj.project.model.Pago;
import com.unaj.project.service.MatriculaService;
import com.unaj.project.service.PagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final MatriculaService matriculaService;

    public PagoController(PagoService pagoService, MatriculaService matriculaService) {
        this.pagoService = pagoService;
        this.matriculaService = matriculaService;
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
        model.addAttribute("cobrado", pagoService.totalCobrado(todos));
        BigDecimal porCobrar = pagoService.totalSaldo(todos, "PENDIENTE").add(pagoService.totalSaldo(todos, "PARCIAL"));
        model.addAttribute("porCobrar", porCobrar);
        model.addAttribute("vencido", pagoService.totalSaldo(todos, "VENCIDO"));
        return "pagos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Pago pago = pagoService.buscarPorId(id);
        model.addAttribute("pago", pago);
        model.addAttribute("abonos", pagoService.listarAbonos(id));
        return "pagos/detalle";
    }

    @PostMapping("/registrar/{id}")
    public String registrar(@PathVariable Long id,
                            @RequestParam BigDecimal monto,
                            @RequestParam(defaultValue = "EFECTIVO") String metodo,
                            Authentication auth,
                            RedirectAttributes ra) {
        String username = (auth != null) ? auth.getName() : null;
        pagoService.registrarAbono(id, monto, metodo, username);
        ra.addFlashAttribute("mensajeExito", "Abono registrado correctamente.");
        return "redirect:/pagos";
    }

    @PostMapping("/nueva-cuota")
    public String nuevaCuota(@RequestParam Long matriculaId,
                             @RequestParam Long alumnoId,
                             @RequestParam String concepto,
                             @RequestParam BigDecimal monto,
                             @RequestParam LocalDate fechaVencimiento,
                             RedirectAttributes ra) {
        matriculaService.agregarCuota(matriculaId, concepto, monto, fechaVencimiento);
        ra.addFlashAttribute("mensajeExito", "Cuota agregada correctamente.");
        return "redirect:/alumnos/" + alumnoId + "/expediente";
    }
}
