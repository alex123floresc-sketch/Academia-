package com.unaj.project.service.impl;

import com.unaj.project.model.*;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.MatriculaDetalleRepository;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.service.MatriculaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MatriculaServiceImpl implements MatriculaService {

    private static final BigDecimal MONTO_MATRICULA = new BigDecimal("150.00");
    private static final BigDecimal MONTO_PENSION   = new BigDecimal("180.00");

    private final MatriculaRepository matriculaRepository;
    private final MatriculaDetalleRepository matriculaDetalleRepository;
    private final AlumnoRepository alumnoRepository;
    private final CicloRepository cicloRepository;
    private final CursoRepository cursoRepository;

    public MatriculaServiceImpl(MatriculaRepository matriculaRepository,
                                MatriculaDetalleRepository matriculaDetalleRepository,
                                AlumnoRepository alumnoRepository,
                                CicloRepository cicloRepository,
                                CursoRepository cursoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.matriculaDetalleRepository = matriculaDetalleRepository;
        this.alumnoRepository = alumnoRepository;
        this.cicloRepository = cicloRepository;
        this.cursoRepository = cursoRepository;
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


    // Sobrecarga de 4 parámetros delegando en el nuevo método de 8 parámetros
    @Override
    @Transactional
    public Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds) {
        return matricular(estudianteId, semestreId, turno, cursoIds,
                "Matrícula", MONTO_MATRICULA,
                "Pensión (1ra cuota)", MONTO_PENSION);
    }

    // Nuevo método implementado con lógica dinámica para pagos personalizados
    @Override
    @Transactional
    public Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds,
                                String conceptoMatricula, BigDecimal montoMatricula,
                                String conceptoPension, BigDecimal montoPension) {

        if (cursoIds == null || cursoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un curso.");
        }

        Set<Long> idsUnicos = new LinkedHashSet<>(cursoIds);
        if (idsUnicos.size() != cursoIds.size()) {
            throw new IllegalArgumentException("La lista de cursos contiene duplicados.");
        }

        if (montoMatricula != null && montoMatricula.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de matrícula debe ser mayor a 0.");
        }
        if (montoPension != null && montoPension.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de pensión no puede ser negativo.");
        }

        Alumno alumno = alumnoRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado: " + estudianteId));

        Ciclo ciclo = cicloRepository.findById(semestreId)
                .orElseThrow(() -> new IllegalArgumentException("Ciclo no encontrado: " + semestreId));

        List<Curso> cursos = cursoRepository.findAllById(idsUnicos);
        if (cursos.size() != idsUnicos.size()) {
            throw new IllegalArgumentException("Uno o más cursos seleccionados no existen.");
        }

        Optional<Matricula> existente =
                matriculaRepository.findByEstudianteIdAndSemestreId(estudianteId, semestreId);

        Matricula matricula;
        boolean esNueva;
        if (existente.isPresent()) {
            matricula = existente.get();

            // Borrar detalles viejos DIRECTO en la BD (por matricula_id) y forzar el DELETE
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
            // Pago de matrícula: usa lo enviado, o el valor por defecto si viene vacío
            String cMat = (conceptoMatricula != null && !conceptoMatricula.isBlank())
                    ? conceptoMatricula : "Matrícula";
            BigDecimal mMat = (montoMatricula != null) ? montoMatricula : MONTO_MATRICULA;
            matricula.addPago(crearPago(cMat, mMat, LocalDate.now()));

            // Pensión: solo se genera si el monto es mayor a 0 (así puedes omitirla)
            if (montoPension != null && montoPension.compareTo(BigDecimal.ZERO) > 0) {
                String cPen = (conceptoPension != null && !conceptoPension.isBlank())
                        ? conceptoPension : "Pensión (1ra cuota)";
                matricula.addPago(crearPago(cPen, montoPension, LocalDate.now().plusDays(30)));
            }
        }

        return matriculaRepository.save(matricula);
    }

    private Pago crearPago(String concepto, BigDecimal monto, LocalDate vencimiento) {
        Pago pago = new Pago();
        pago.setConcepto(concepto);
        pago.setMonto(monto);
        pago.setFechaVencimiento(vencimiento);
        pago.setEstado("PENDIENTE");
        return pago;
    }

    @Override
    @Transactional
    public void anular(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula no encontrada: " + matriculaId));
        matricula.setEstado("ANULADA");
        matriculaRepository.save(matricula);
    }

    @Override
    public void eliminar(Long id) {
        matriculaRepository.deleteById(id);
    }
}