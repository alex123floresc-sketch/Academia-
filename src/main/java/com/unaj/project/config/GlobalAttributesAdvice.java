package com.unaj.project.config;

import com.unaj.project.dto.NotificacionDTO;
import com.unaj.project.service.PagoService;
import com.unaj.project.service.ProfesorService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalAttributesAdvice {

    private final PagoService pagoService;
    private final ProfesorService profesorService;

    public GlobalAttributesAdvice(PagoService pagoService, ProfesorService profesorService) {
        this.pagoService = pagoService;
        this.profesorService = profesorService;
    }

    @ModelAttribute
    public void agregarNotificaciones(Authentication auth, Model model) {
        List<NotificacionDTO> notificaciones = new ArrayList<>();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && tieneAlgunRol(auth, "ROLE_ADMIN", "ROLE_CAJERO")) {

            long pagosVencidos = pagoService.listarTodos().stream()
                    .filter(p -> "VENCIDO".equals(p.getEstado())).count();
            if (pagosVencidos > 0) {
                notificaciones.add(new NotificacionDTO(
                        "Pagos vencidos",
                        pagosVencidos + " pago" + (pagosVencidos == 1 ? "" : "s") + " vencido" + (pagosVencidos == 1 ? "" : "s"),
                        "/pagos"));
            }

            long profesoresSinTarifa = profesorService.listarTodos().stream()
                    .filter(p -> p.getTarifaHora() == null).count();
            if (profesoresSinTarifa > 0) {
                notificaciones.add(new NotificacionDTO(
                        "Profesores sin tarifa",
                        profesoresSinTarifa + " profesor" + (profesoresSinTarifa == 1 ? "" : "es") + " sin tarifa por hora configurada",
                        "/profesores"));
            }
        }

        model.addAttribute("notificaciones", notificaciones);
    }

    private boolean tieneAlgunRol(Authentication auth, String... roles) {
        List<String> buscados = List.of(roles);
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (buscados.contains(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
