package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Pago;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final PagoRepository pagoRepository;
    private final MatriculaRepository matriculaRepository;
    private final QrCodeService qrCodeService;

    public AlumnoController(AlumnoService alumnoService, PagoRepository pagoRepository,
                            MatriculaRepository matriculaRepository, QrCodeService qrCodeService) {
        this.alumnoService = alumnoService;
        this.pagoRepository = pagoRepository;
        this.matriculaRepository = matriculaRepository;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 15, sort = "apellido") Pageable pageable,
                         Model model) {
        java.util.Map<Long, Long> deuda = new java.util.HashMap<>();
        for (Object[] fila : pagoRepository.contarDeudaPorAlumno()) {
            deuda.put((Long) fila[0], (Long) fila[1]);
        }
        Page<Alumno> pagina = alumnoService.buscarPagina(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("alumnos", pagina.getContent());
        model.addAttribute("deuda", deuda);
        model.addAttribute("q", q);
        return "alumnos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("alumnoForm", new AlumnoForm());
        return "alumnos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("alumnoForm") AlumnoForm alumnoForm,
                          BindingResult result,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "alumnos/formulario";   // remuestra con los mensajes de error
        }
        alumnoService.guardar(alumnoForm);
        ra.addFlashAttribute("mensajeExito", "Alumno guardado correctamente.");
        return "redirect:/alumnos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("alumnoForm", alumnoService.buscarFormPorId(id));
        return "alumnos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        alumnoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Alumno eliminado correctamente.");
        return "redirect:/alumnos";
    }

    @GetMapping("/{id}/expediente")
    public String expediente(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(id);

        Map<Long, List<Pago>> pagosPorMatricula = new LinkedHashMap<>();
        for (Matricula m : matriculas) {
            pagosPorMatricula.put(m.getId(), pagoRepository.findByMatriculaId(m.getId()));
        }

        model.addAttribute("alumno", alumno);
        model.addAttribute("matriculas", matriculas);
        model.addAttribute("pagosPorMatricula", pagosPorMatricula);
        return "alumnos/expediente";
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        Alumno alumno = alumnoService.buscarPorId(id);
        byte[] png = qrCodeService.generarPng("ALU-" + alumno.getId(), 320);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/{id}/qr-view")
    public String qrView(@PathVariable Long id, Model model) {
        model.addAttribute("alumno", alumnoService.buscarPorId(id));
        return "alumnos/qr";
    }
}