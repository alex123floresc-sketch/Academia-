package com.unaj.project.controller;

import com.unaj.project.exception.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarNoEncontrado(RecursoNoEncontradoException ex,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeModulo(request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String manejarArgInvalido(RuntimeException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeModulo(request);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public String manejarNoEncontradoEnBorrado(EmptyResultDataAccessException ex,
                                               HttpServletRequest request,
                                               RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", "El registro que intentas eliminar ya no existe.");
        return "redirect:" + rutaBaseDeModulo(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String manejarIntegridad(DataIntegrityViolationException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError",
                "No se pudo completar la operación: el registro está en uso por otros datos del sistema " +
                        "(pagos, asistencias u otra información relacionada).");
        return "redirect:" + rutaBaseDeModulo(request);
    }

    private String rutaBaseDeModulo(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] modulos = {"/alumnos", "/cursos", "/ciclos", "/profesores",
                "/horarios", "/matriculas", "/pagos", "/usuarios",
                "/reportes", "/asistencias", "/areas", "/horas-docentes", "/resumen",
                "/configuracion", "/actividad", "/sedes", "/faq", "/testimonios",
                "/logros-ingreso", "/galeria", "/calendario", "/solicitudes", "/pasos-admision"};
        for (String m : modulos) {
            if (uri.startsWith(m)) {
                return m + contextoDeNavegacion(m, request);
            }
        }
        return "/inicio";
    }

    /**
     * Algunos módulos (hoy solo Horarios) tienen navegación en capas (ciclo/nivel/área):
     * si una acción falla, volver a la ruta base "pelada" tira al usuario hasta arriba de
     * esa navegación en vez de dejarlo donde estaba. Si la request que falló traía esos
     * parámetros (los formularios de asignar/quitar curso y crear/eliminar bloque siempre
     * los incluyen), se reconstruyen aquí para volver exactamente a la misma grilla.
     */
    private String contextoDeNavegacion(String modulo, HttpServletRequest request) {
        if (!"/horarios".equals(modulo)) {
            return "";
        }
        String cicloId = request.getParameter("cicloId");
        String nivel = request.getParameter("nivel");
        String area = request.getParameter("area");
        if (cicloId == null || nivel == null || area == null || area.isBlank()) {
            return "";
        }
        return "?cicloId=" + cicloId + "&nivel=" + nivel
                + "&area=" + URLEncoder.encode(area, StandardCharsets.UTF_8);
    }
}