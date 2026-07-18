package com.unaj.project.controller;

import com.unaj.project.exception.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Recurso inexistente (id que no existe): volvemos a la lista del módulo con aviso
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarNoEncontrado(RecursoNoEncontradoException ex,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeModulo(request);
    }

    // Argumentos inválidos o estados no permitidos (ej. reglas de negocio)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String manejarArgInvalido(RuntimeException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeModulo(request);
    }

    /**
     * Deduce la ruta base del módulo a partir de la URL.
     * Ej: "/alumnos/editar/999" -> "/alumnos". Si no se reconoce, va a /inicio.
     */
    private String rutaBaseDeModulo(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] modulos = {"/alumnos", "/cursos", "/ciclos", "/profesores",
                "/horarios", "/matriculas", "/pagos", "/usuarios",
                "/reportes", "/asistencias"};
        for (String m : modulos) {
            if (uri.startsWith(m)) {
                return m;
            }
        }
        return "/inicio";
    }
}