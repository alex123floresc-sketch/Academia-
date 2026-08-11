package com.unaj.project.service.impl;

import com.unaj.project.model.*;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.MatriculaDetalleRepository;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.MatriculaService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final MatriculaDetalleRepository matriculaDetalleRepository;
    private final AlumnoRepository alumnoRepository;
    private final CicloRepository cicloRepository;
    private final CursoRepository cursoRepository;
    private final ConfiguracionService configuracionService;
    private final RegistroActividadService registroActividadService;

    public MatriculaServiceImpl(MatriculaRepository matriculaRepository,
                                MatriculaDetalleRepository matriculaDetalleRepository,
                                AlumnoRepository alumnoRepository,
                                CicloRepository cicloRepository,
                                CursoRepository cursoRepository,
                                ConfiguracionService configuracionService,
                                RegistroActividadService registroActividadService) {
        this.matriculaRepository = matriculaRepository;
        this.matriculaDetalleRepository = matriculaDetalleRepository;
        this.alumnoRepository = alumnoRepository;
        this.cicloRepository = cicloRepository;
        this.cursoRepository = cursoRepository;
        this.configuracionService = configuracionService;
        this.registroActividadService = registroActividadService;
    }
    @Override
    public List<Matricula> listarTodos() {
        return matriculaRepository.findAllConEstudianteYSemestre();
    }

    @Override
    public Matricula buscarPorId(Long id) {
        return matriculaRepository.findById(id).orElse(null);
    }

    @Override
    public Matricula buscarFichaPorId(Long id) {
        return matriculaRepository.findByIdConDetalle(id).orElse(null);
    }

    @Override
    public List<Matricula> listarPorEstudiante(Long estudianteId) {
        return matriculaRepository.findByEstudianteId(estudianteId);
    }


    @Override
    @Transactional
    public Matricula matricular(Long estudianteId, Long semestreId, Turno turno, String area) {
        Configuracion configuracion = configuracionService.obtener();
        return matricular(estudianteId, semestreId, turno, area,
                "Matrícula", configuracion.getMontoMatricula(),
                "Pensión", configuracion.getMontoPension());
    }

    @Override
    @Transactional
    public Matricula matricular(Long estudianteId, Long semestreId, Turno turno, String area,
                                String conceptoMatricula, BigDecimal montoMatricula,
                                String conceptoPension, BigDecimal montoPension) {
        return matricular(estudianteId, semestreId, turno, area,
                conceptoMatricula, montoMatricula, conceptoPension, montoPension, null);
    }

    @Override
    @Transactional
    public Matricula matricular(Long estudianteId, Long semestreId, Turno turno, String area,
                                String conceptoMatricula, BigDecimal montoMatricula,
                                String conceptoPension, BigDecimal montoPension, Integer numeroCuotas) {

        if (area == null || area.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un área.");
        }

        if (montoMatricula != null && montoMatricula.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de matrícula debe ser mayor a 0.");
        }
        if (montoPension != null && montoPension.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de pensión no puede ser negativo.");
        }
        if (numeroCuotas != null && numeroCuotas < 1) {
            throw new IllegalArgumentException("El número de cuotas debe ser al menos 1.");
        }

        Alumno alumno = alumnoRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado: " + estudianteId));

        if (alumno.getNivel() == null) {
            throw new IllegalArgumentException("El alumno no tiene un nivel asignado.");
        }

        Ciclo ciclo = cicloRepository.findById(semestreId)
                .orElseThrow(() -> new IllegalArgumentException("Ciclo no encontrado: " + semestreId));

        List<Curso> cursos = cursoRepository.findByNivelAndArea(alumno.getNivel(), area);
        if (cursos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El área " + area + " todavía no tiene cursos asignados. Ve a Áreas para agregarlos.");
        }

        Optional<Matricula> existente =
                matriculaRepository.findByEstudianteIdAndSemestreId(estudianteId, semestreId);

        Matricula matricula;
        boolean esNueva;
        if (existente.isPresent()) {
            matricula = existente.get();

            matriculaDetalleRepository.deleteByMatriculaId(matricula.getId());
            matriculaDetalleRepository.flush();
            matricula.getDetalles().clear();

            matricula.setEstado("ACTIVA");
            matricula.setFechaMatricula(LocalDateTime.now());
            matricula.setTurno(turno);
            esNueva = false;
        } else {
            matricula = new Matricula();
            matricula.setEstudiante(alumno);
            matricula.setSemestre(ciclo);
            matricula.setTurno(turno);
            matricula.setFechaMatricula(LocalDateTime.now());
            matricula.setEstado("ACTIVA");
            esNueva = true;
        }

        for (Curso curso : cursos) {
            matricula.addDetalle(new MatriculaDetalle(curso));
        }

        if (esNueva) {
            Configuracion configuracion = configuracionService.obtener();
            String cMat = (conceptoMatricula != null && !conceptoMatricula.isBlank())
                    ? conceptoMatricula : "Matrícula";
            BigDecimal mMat = (montoMatricula != null) ? montoMatricula : configuracion.getMontoMatricula();
            matricula.addPago(crearPago(cMat, mMat, LocalDate.now()));

            if (montoPension != null && montoPension.compareTo(BigDecimal.ZERO) > 0) {
                String baseConcepto = (conceptoPension != null && !conceptoPension.isBlank())
                        ? conceptoPension : "Pensión";
                int cuotasAGenerar = (numeroCuotas != null) ? numeroCuotas : configuracion.getNumeroCuotasPension();
                int diasEntreCuotas = configuracion.getDiasEntreCuotas();

                for (int n = 1; n <= cuotasAGenerar; n++) {
                    String cPen = (cuotasAGenerar > 1) ? baseConcepto + " (cuota " + n + "/" + cuotasAGenerar + ")" : baseConcepto;
                    LocalDate vencimiento = LocalDate.now().plusDays((long) diasEntreCuotas * n);
                    matricula.addPago(crearPago(cPen, montoPension, vencimiento));
                }
            }
        }

        Matricula guardada = matriculaRepository.save(matricula);
        registroActividadService.registrar(esNueva ? TipoAccion.CREAR : TipoAccion.EDITAR, "Matrículas",
                guardada.getId(), (esNueva ? "Matriculó a " : "Renovó la matrícula de ")
                        + alumno.getNombre() + " " + alumno.getApellido() + " en " + ciclo.getNombre());
        return guardada;
    }

    private Pago crearPago(String concepto, BigDecimal monto, LocalDate vencimiento) {
        Pago pago = new Pago();
        pago.setConcepto(concepto);
        pago.setMonto(monto);
        pago.setMontoPagado(BigDecimal.ZERO);
        pago.setFechaVencimiento(vencimiento);
        pago.setEstado("PENDIENTE");
        return pago;
    }

    @Override
    @Transactional
    public Pago agregarCuota(Long matriculaId, String concepto, BigDecimal monto, LocalDate vencimiento) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto de la cuota es obligatorio.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la cuota debe ser mayor a 0.");
        }
        if (vencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));

        Pago pago = crearPago(concepto, monto, vencimiento);
        matricula.addPago(pago);
        matriculaRepository.save(matricula);
        registroActividadService.registrar(TipoAccion.CREAR, "Matrículas", matricula.getId(),
                "Agregó la cuota \"" + concepto + "\" (" + configuracionService.obtener().getSimboloMoneda() + " " + monto + ") a " + matricula.getEstudiante().getNombre()
                        + " " + matricula.getEstudiante().getApellido());
        return pago;
    }

    @Override
    @Transactional
    public void anular(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));
        matricula.setEstado("ANULADA");
        matriculaRepository.save(matricula);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Matrículas", matricula.getId(),
                "Anuló la matrícula de " + matricula.getEstudiante().getNombre() + " " + matricula.getEstudiante().getApellido());
    }

}