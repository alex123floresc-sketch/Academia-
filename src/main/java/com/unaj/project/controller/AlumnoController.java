package com.unaj.project.controller;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Pago;
import com.unaj.project.model.Turno;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.dto.AsistenciaResumenAlumnoDTO;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.AsistenciaService;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.MatriculaService;
import com.unaj.project.service.PdfGeneradorService;
import com.unaj.project.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.util.Base64;
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
    private final PdfGeneradorService pdfGeneradorService;
    private final MatriculaService matriculaService;
    private final CicloService cicloService;
    private final ConfiguracionService configuracionService;
    private final AsistenciaService asistenciaService;

    public AlumnoController(AlumnoService alumnoService, PagoRepository pagoRepository,
                            MatriculaRepository matriculaRepository, QrCodeService qrCodeService,
                            PdfGeneradorService pdfGeneradorService, MatriculaService matriculaService,
                            CicloService cicloService,
                            ConfiguracionService configuracionService,
                            AsistenciaService asistenciaService) {
        this.alumnoService = alumnoService;
        this.pagoRepository = pagoRepository;
        this.matriculaRepository = matriculaRepository;
        this.qrCodeService = qrCodeService;
        this.pdfGeneradorService = pdfGeneradorService;
        this.matriculaService = matriculaService;
        this.cicloService = cicloService;
        this.configuracionService = configuracionService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Nivel nivel,
                         @RequestParam(required = false) String area,
                         @PageableDefault(size = 15, sort = "apellido") Pageable pageable,
                         Model model) {
        Nivel nivelSel = (nivel != null) ? nivel : Nivel.PREUNIVERSITARIO;
        List<String> areasDelNivel = Areas.paraNivel(nivelSel);
        String areaSel = (area != null && areasDelNivel.contains(area)) ? area : null;

        java.util.Map<Long, Long> deuda = new java.util.HashMap<>();
        for (Object[] fila : pagoRepository.contarDeudaPorAlumno()) {
            deuda.put((Long) fila[0], (Long) fila[1]);
        }
        Page<Alumno> pagina = alumnoService.buscarPagina(q, nivelSel, areaSel, pageable);
        long totalAlumnos = alumnoService.listarTodos().stream()
                .filter(a -> a.getNivel() == nivelSel).count();
        long totalConDeuda = alumnoService.listarTodos().stream()
                .filter(a -> a.getNivel() == nivelSel)
                .filter(a -> deuda.getOrDefault(a.getId(), 0L) > 0)
                .count();
        model.addAttribute("pagina", pagina);
        model.addAttribute("alumnos", pagina.getContent());
        model.addAttribute("deuda", deuda);
        model.addAttribute("q", q);
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("nivel", nivelSel);
        model.addAttribute("area", areaSel);
        model.addAttribute("areas", areasDelNivel);
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalConDeuda", totalConDeuda);
        model.addAttribute("totalAlDia", totalAlumnos - totalConDeuda);
        return "alumnos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("alumnoForm", new AlumnoForm());
        cargarDatosMatricula(model);
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("areasPorNivel", areasPorNivelJson());
        return "alumnos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("alumnoForm") AlumnoForm alumnoForm,
                          BindingResult result,
                          @RequestParam(required = false) Long cicloId,
                          @RequestParam(required = false) Turno turno,
                          @RequestParam(required = false) String conceptoMatricula,
                          @RequestParam(required = false) java.math.BigDecimal montoMatricula,
                          @RequestParam(required = false) String conceptoPension,
                          @RequestParam(required = false) java.math.BigDecimal montoPension,
                          @RequestParam(required = false) Integer numeroCuotas,
                          Model model,
                          RedirectAttributes ra) {
        boolean esNuevo = alumnoForm.getId() == null;
        if (result.hasErrors()) {
            if (esNuevo) {
                cargarDatosMatricula(model);
            }
            model.addAttribute("niveles", Nivel.values());
            model.addAttribute("areasPorNivel", areasPorNivelJson());
            return "alumnos/formulario";
        }
        Alumno alumno = alumnoService.guardar(alumnoForm);
        if (esNuevo) {
            if (cicloId == null || turno == null) {
                throw new IllegalArgumentException(
                        "El alumno se guardó, pero para matricularlo debes elegir ciclo y turno.");
            }
            matriculaService.matricular(alumno.getId(), cicloId, turno, alumnoForm.getArea(),
                    conceptoMatricula, montoMatricula, conceptoPension, montoPension, numeroCuotas);
            ra.addFlashAttribute("mensajeExito", "Alumno guardado y matriculado correctamente.");
        } else {
            ra.addFlashAttribute("mensajeExito", "Alumno guardado correctamente.");
        }
        return "redirect:/alumnos?nivel=" + alumno.getNivel().name();
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("alumnoForm", alumnoService.buscarFormPorId(id));
        model.addAttribute("tieneFoto", alumnoService.buscarPorId(id).isFotoPresente());
        model.addAttribute("niveles", Nivel.values());
        model.addAttribute("areasPorNivel", areasPorNivelJson());
        return "alumnos/formulario";
    }

    private void cargarDatosMatricula(Model model) {
        model.addAttribute("ciclos", cicloService.listarTodos());
        model.addAttribute("cicloActivo", cicloService.obtenerActivo());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("configuracion", configuracionService.obtener());
    }

    private Map<String, List<String>> areasPorNivelJson() {
        Map<String, List<String>> mapa = new java.util.LinkedHashMap<>();
        for (Nivel n : Nivel.values()) {
            mapa.put(n.name(), Areas.paraNivel(n));
        }
        return mapa;
    }

    @GetMapping("/{id}/matricular")
    public String nuevaMatricula(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        model.addAttribute("alumno", alumno);
        model.addAttribute("areas", Areas.paraNivel(alumno.getNivel()));
        cargarDatosMatricula(model);
        return "alumnos/matricular";
    }

    @PostMapping("/{id}/matricular")
    public String guardarMatricula(@PathVariable Long id,
                                   @RequestParam Long cicloId,
                                   @RequestParam Turno turno,
                                   @RequestParam String area,
                                   @RequestParam(required = false) String conceptoMatricula,
                                   @RequestParam(required = false) java.math.BigDecimal montoMatricula,
                                   @RequestParam(required = false) String conceptoPension,
                                   @RequestParam(required = false) java.math.BigDecimal montoPension,
                                   @RequestParam(required = false) Integer numeroCuotas,
                                   Model model,
                                   RedirectAttributes ra) {
        try {
            matriculaService.matricular(id, cicloId, turno, area,
                    conceptoMatricula, montoMatricula, conceptoPension, montoPension, numeroCuotas);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alumno alumno = alumnoService.buscarPorId(id);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("alumno", alumno);
            model.addAttribute("areas", Areas.paraNivel(alumno.getNivel()));
            cargarDatosMatricula(model);
            return "alumnos/matricular";
        }
        ra.addFlashAttribute("mensajeExito", "Alumno matriculado correctamente.");
        return "redirect:/alumnos/" + id + "/expediente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, @RequestParam(required = false) Nivel nivel, RedirectAttributes ra) {
        alumnoService.eliminar(id);
        ra.addFlashAttribute("mensajeExito", "Alumno eliminado correctamente.");
        return "redirect:/alumnos" + (nivel != null ? "?nivel=" + nivel.name() : "");
    }

    @GetMapping("/{id}/expediente")
    public String expediente(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(id);

        Map<Long, List<Pago>> pagosPorMatricula = new LinkedHashMap<>();
        java.math.BigDecimal totalPagado = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalPendiente = java.math.BigDecimal.ZERO;
        long matriculasActivas = 0;
        for (Matricula m : matriculas) {
            List<Pago> pagos = pagoRepository.findByMatriculaId(m.getId());
            pagosPorMatricula.put(m.getId(), pagos);
            for (Pago p : pagos) {
                totalPagado = totalPagado.add(p.getMontoPagado());
                totalPendiente = totalPendiente.add(p.getSaldo());
            }
            if ("ACTIVA".equals(m.getEstado())) {
                matriculasActivas++;
            }
        }

        model.addAttribute("alumno", alumno);
        model.addAttribute("matriculas", matriculas);
        model.addAttribute("pagosPorMatricula", pagosPorMatricula);
        model.addAttribute("totalPagado", totalPagado);
        model.addAttribute("totalPendiente", totalPendiente);
        model.addAttribute("matriculasActivas", matriculasActivas);
        return "alumnos/expediente";
    }

    @GetMapping("/{id}/asistencia")
    public String asistencia(@PathVariable Long id, @RequestParam(required = false) Long cicloId, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(id);
        AsistenciaResumenAlumnoDTO resumen = asistenciaService.resumenPorAlumno(id, cicloId);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matriculas", matriculas);
        model.addAttribute("resumen", resumen);
        return "alumnos/asistencia";
    }

    @GetMapping("/{id}/asistencia/pdf")
    public ResponseEntity<byte[]> asistenciaPdf(@PathVariable Long id, @RequestParam(required = false) Long cicloId) {
        Alumno alumno = alumnoService.buscarPorId(id);
        AsistenciaResumenAlumnoDTO resumen = asistenciaService.resumenPorAlumno(id, cicloId);

        Context context = new Context();
        context.setVariable("alumno", alumno);
        context.setVariable("resumen", resumen);

        byte[] pdfBytes = pdfGeneradorService.renderizar("alumnos/asistencia-pdf", context);
        String filename = "asistencia_" + alumno.getId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        Alumno alumno = alumnoService.buscarPorId(id);
        byte[] png = qrCodeService.generarPng("ALU-" + alumno.getId(), 320);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/{id}/foto")
    @ResponseBody
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        Alumno alumno = alumnoService.buscarPorId(id);
        if (!alumno.isFotoPresente()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = (alumno.getFotoContentType() != null)
                ? MediaType.parseMediaType(alumno.getFotoContentType())
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(alumno.getFoto());
    }

    @GetMapping("/{id}/carnet")
    public String carnet(@PathVariable Long id, Model model) {
        cargarDatosCarnet(id, model);
        return "alumnos/carnet";
    }

    @GetMapping("/{id}/carnet/imprimir")
    public String carnetImprimir(@PathVariable Long id, Model model) {
        cargarDatosCarnet(id, model);
        return "alumnos/carnet-imprimir";
    }

    @GetMapping("/{id}/carnet/pdf")
    public ResponseEntity<byte[]> carnetPdf(@PathVariable Long id) throws Exception {
        Alumno alumno = alumnoService.buscarPorId(id);
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(id);
        Matricula matriculaVigente = matriculas.isEmpty() ? null : matriculas.get(0);

        String fotoDataUri = null;
        if (alumno.isFotoPresente()) {
            String tipo = (alumno.getFotoContentType() != null) ? alumno.getFotoContentType() : "image/jpeg";
            fotoDataUri = "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(alumno.getFoto());
        }
        byte[] qrPng = qrCodeService.generarPng("ALU-" + alumno.getId(), 320);
        String qrDataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrPng);

        Context context = new Context();
        context.setVariable("alumno", alumno);
        context.setVariable("matriculaVigente", matriculaVigente);
        context.setVariable("fotoDataUri", fotoDataUri);
        context.setVariable("qrDataUri", qrDataUri);

        byte[] pdfBytes = pdfGeneradorService.renderizar("alumnos/carnet-pdf", context);
        String filename = "carnet_" + alumno.getId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
    }

    private void cargarDatosCarnet(Long id, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(id);
        Matricula matriculaVigente = matriculas.isEmpty() ? null : matriculas.get(0);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matriculaVigente", matriculaVigente);
    }
}