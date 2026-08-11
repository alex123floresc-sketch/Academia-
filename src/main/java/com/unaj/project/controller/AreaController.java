package com.unaj.project.controller;

import com.unaj.project.dto.AreaResumenDTO;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Nivel;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.CursoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/areas")
public class AreaController {

    private final CursoService cursoService;
    private final AlumnoService alumnoService;

    public AreaController(CursoService cursoService, AlumnoService alumnoService) {
        this.cursoService = cursoService;
        this.alumnoService = alumnoService;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) Nivel nivel,
                      @RequestParam(required = false) String area, Model model) {
        if (nivel == null) {
            model.addAttribute("resumenNiveles", cursoService.contarPorNivel());
            return "areas/niveles";
        }

        List<String> areasDelNivel = Areas.paraNivel(nivel);
        String areaSel = (area != null && areasDelNivel.contains(area)) ? area : null;

        List<Curso> cursos = cursoService.listarPorNivel(nivel);

        if (areaSel == null) {
            Map<String, Long> alumnosPorArea = alumnoService.contarPorArea(nivel);
            List<AreaResumenDTO> resumenes = areasDelNivel.stream()
                    .map(a -> new AreaResumenDTO(
                            a,
                            cursos.stream().filter(c -> c.getAreas().contains(a)).count(),
                            cursos.stream().filter(c -> c.getAreas().contains(a) && c.getProfesor() != null)
                                    .map(c -> c.getProfesor().getId()).distinct().count(),
                            alumnosPorArea.get(a)))
                    .toList();
            model.addAttribute("nivel", nivel);
            model.addAttribute("resumenes", resumenes);
            return "areas/lista";
        }

        List<Curso> cursosOrdenados = cursos.stream()
                .sorted(Comparator.comparing((Curso c) -> !c.getAreas().contains(areaSel))
                        .thenComparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("nivel", nivel);
        model.addAttribute("areaSeleccionada", areaSel);
        model.addAttribute("cursos", cursosOrdenados);
        return "areas/gestionar";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Nivel nivel,
                          @RequestParam String area,
                          @RequestParam(required = false) List<Long> cursoIds,
                          RedirectAttributes ra) {
        cursoService.establecerCursosDeArea(nivel, area, cursoIds);
        ra.addFlashAttribute("mensajeExito", "Cursos de " + area + " actualizados correctamente.");
        return "redirect:/areas?nivel=" + nivel.name() + "&area=" + area;
    }
}
