package com.unaj.project.controller;

import com.unaj.project.dto.SolicitudInformacionForm;
import com.unaj.project.model.Configuracion;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Nivel;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.EventoAcademicoService;
import com.unaj.project.service.FotoGaleriaService;
import com.unaj.project.service.LogroIngresoService;
import com.unaj.project.service.PasoAdmisionService;
import com.unaj.project.service.PreguntaFrecuenteService;
import com.unaj.project.service.ProfesorService;
import com.unaj.project.service.SedeService;
import com.unaj.project.service.TestimonioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {

    private final ConfiguracionService configuracionService;
    private final ProfesorService profesorService;
    private final CursoService cursoService;
    private final SedeService sedeService;
    private final PreguntaFrecuenteService preguntaFrecuenteService;
    private final TestimonioService testimonioService;
    private final LogroIngresoService logroIngresoService;
    private final FotoGaleriaService fotoGaleriaService;
    private final EventoAcademicoService eventoAcademicoService;
    private final PasoAdmisionService pasoAdmisionService;
    private final AlumnoService alumnoService;

    public LoginController(ConfiguracionService configuracionService, ProfesorService profesorService,
                           CursoService cursoService, SedeService sedeService,
                           PreguntaFrecuenteService preguntaFrecuenteService, TestimonioService testimonioService,
                           LogroIngresoService logroIngresoService, FotoGaleriaService fotoGaleriaService,
                           EventoAcademicoService eventoAcademicoService, PasoAdmisionService pasoAdmisionService,
                           AlumnoService alumnoService) {
        this.configuracionService = configuracionService;
        this.profesorService = profesorService;
        this.cursoService = cursoService;
        this.sedeService = sedeService;
        this.preguntaFrecuenteService = preguntaFrecuenteService;
        this.testimonioService = testimonioService;
        this.logroIngresoService = logroIngresoService;
        this.fotoGaleriaService = fotoGaleriaService;
        this.eventoAcademicoService = eventoAcademicoService;
        this.pasoAdmisionService = pasoAdmisionService;
        this.alumnoService = alumnoService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        // La página pública es larga: el buffer de respuesta puede comitear antes de que Thymeleaf
        // llegue al <form> de "Solicita información". Si el token CSRF (por defecto diferido) recién
        // se resuelve ahí, Spring Security intenta crear la sesión con la respuesta ya comiteada y
        // lanza IllegalStateException. Forzamos la resolución acá, antes de renderizar nada.
        Object csrfAttr = request.getAttribute(CsrfToken.class.getName());
        if (csrfAttr instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }
        Configuracion configuracion = configuracionService.obtener();
        model.addAttribute("configuracion", configuracion);
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("docentes", configuracion.isMostrarDocentes() ? profesorService.listarDestacados() : List.of());
        model.addAttribute("cursosDestacados", configuracion.isMostrarCursos() ? cursoService.listarDestacados() : List.of());
        Map<Nivel, Map<String, List<Curso>>> mallaCursos = new LinkedHashMap<>();
        if (configuracion.isMostrarCursos()) {
            for (Nivel nivel : Nivel.values()) {
                Map<String, List<Curso>> malla = cursoService.mallaPorNivel(nivel);
                if (!malla.isEmpty()) {
                    mallaCursos.put(nivel, malla);
                }
            }
        }
        model.addAttribute("mallaCursos", mallaCursos);
        model.addAttribute("sedes", configuracion.isMostrarSedes() ? sedeService.listarActivas() : List.of());
        model.addAttribute("preguntas", configuracion.isMostrarFaq() ? preguntaFrecuenteService.listarActivas() : List.of());
        model.addAttribute("testimonios", configuracion.isMostrarTestimonios() ? testimonioService.listarActivos() : List.of());
        model.addAttribute("logros", configuracion.isMostrarLogros() ? logroIngresoService.listarActivos() : List.of());
        model.addAttribute("fotosGaleria", configuracion.isMostrarGaleria() ? fotoGaleriaService.listarActivas() : List.of());
        model.addAttribute("eventos", configuracion.isMostrarCalendario() ? eventoAcademicoService.listarProximos() : List.of());
        model.addAttribute("pasosAdmision", configuracion.isMostrarProcesoAdmision() ? pasoAdmisionService.listarActivos() : List.of());
        Integer anosExperiencia = (configuracion.getAnioFundacion() != null)
                ? Math.max(0, Year.now().getValue() - configuracion.getAnioFundacion())
                : null;
        model.addAttribute("anosExperiencia", anosExperiencia);

        long totalAlumnos = alumnoService.contarPorNivel().values().stream().mapToLong(Long::longValue).sum();
        long totalCursos = cursoService.contarPorNivel().values().stream().mapToLong(Long::longValue).sum();
        long totalSedes = sedeService.listarActivas().size();
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalSedes", totalSedes);

        if (!model.containsAttribute("solicitudForm")) {
            model.addAttribute("solicitudForm", new SolicitudInformacionForm());
        }
        return "publico/inicio";
    }

}
