package com.unaj.project.service;

import com.unaj.project.model.Matricula;
import com.unaj.project.model.Turno;

import java.util.List;
import java.math.BigDecimal;

public interface MatriculaService {

    List<Matricula> listarTodos();

    Matricula buscarPorId(Long id);

    Matricula buscarFichaPorId(Long id);

    List<Matricula> listarPorEstudiante(Long estudianteId);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds);

    Matricula matricular(Long estudianteId, Long semestreId, Turno turno, List<Long> cursoIds,
                         String conceptoMatricula, BigDecimal montoMatricula,
                         String conceptoPension, BigDecimal montoPension);

    void anular(Long matriculaId);

    void eliminar(Long id);
}