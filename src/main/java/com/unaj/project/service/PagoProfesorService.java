package com.unaj.project.service;

import com.unaj.project.model.PagoProfesor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PagoProfesorService {

    PagoProfesor registrar(Long profesorId, String tipoPeriodo, LocalDate periodoInicio, LocalDate periodoFin,
                           BigDecimal horasPagadas, BigDecimal monto, LocalDate fechaPago, String metodo,
                           String observaciones, String username);

    List<PagoProfesor> listarPorProfesor(Long profesorId);
}
