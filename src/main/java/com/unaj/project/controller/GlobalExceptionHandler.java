package com.unaj.project.controller;

import com.unaj.project.exception.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
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

    // deleteById sobre un id que ya no existe (doble clic, enlace obsoleto, etc.)
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public String manejarNoEncontradoEnBorrado(EmptyResultDataAccessException ex,
                                               HttpServletRequest request,
                                               RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError", "El registro que intentas eliminar ya no existe.");
        return "redirect:" + rutaBaseDeModulo(request);
    }

    // Violación de integridad referencial (ej. borrar una matrícula con pagos abonados,
    // un usuario que registró abonos/asistencias, o un horario con asistencias tomadas)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String manejarIntegridad(DataIntegrityViolationException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        ra.addFlashAttribute("mensajeError",
                "No se pudo completar la operación: el registro está en uso por otros datos del sistema " +
                        "(pagos, asistencias u otra información relacionada).");
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