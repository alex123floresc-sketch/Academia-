package com.unaj.project.service;

import com.unaj.project.model.Abono;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PagoService {
    List<Pago> listarTodos();
    Pago buscarPorId(Long id);
    List<Pago> listarPorMatricula(Long matriculaId);

    Page<Alumno> buscarAlumnosConPagos(String q, Pageable pageable);

    Map<Long, List<Pago>> agruparPorAlumno(List<Long> alumnoIds);

    List<Abono> listarAbonos(Long pagoId);

    Pago registrarAbono(Long pagoId, BigDecimal monto, String metodo, String username);

    BigDecimal totalPorEstado(List<Pago> pagos, String estado);

    BigDecimal totalCobrado(List<Pago> pagos);

    BigDecimal totalSaldo(List<Pago> pagos, String estado);

    int marcarVencidos();
}
