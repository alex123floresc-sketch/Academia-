package com.unaj.project.service;

import com.unaj.project.model.Matricula;
import com.unaj.project.model.Pago;
import com.unaj.project.model.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public interface MatriculaService {

    List<Matricula> listarTodos();

    Page<Matricula> buscarPagina(String q, Pageable pageable);

    Matricula buscarPorId(Long id);

    Matricula buscarFichaPorId(Long id);

    List<Matricula> listarPorEstudiante(Long estudianteId);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds,
                         String conceptoMatricula, BigDecimal montoMatricula,
                         String conceptoPension, BigDecimal montoPension);

    void anular(Long matriculaId);

    void eliminar(Long id);

    /** Agrega una nueva cuota (Pago) a una matrícula ya existente, p. ej. la pensión del siguiente mes. */
    Pago agregarCuota(Long matriculaId, String concepto, BigDecimal monto, LocalDate vencimiento);
}