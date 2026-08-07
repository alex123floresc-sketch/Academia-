package com.unaj.project.service;

import com.unaj.project.model.Matricula;
import com.unaj.project.model.Pago;
import com.unaj.project.model.Turno;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public interface MatriculaService {

    List<Matricula> listarTodos();

    Matricula buscarPorId(Long id);

    Matricula buscarFichaPorId(Long id);

    List<Matricula> listarPorEstudiante(Long estudianteId);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, String area);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, String area,
                         String conceptoMatricula, BigDecimal montoMatricula,
                         String conceptoPension, BigDecimal montoPension);

    void anular(Long matriculaId);

    Pago agregarCuota(Long matriculaId, String concepto, BigDecimal monto, LocalDate vencimiento);
}