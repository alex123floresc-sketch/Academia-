package com.unaj.project.service;

import com.unaj.project.model.Pago;
import java.math.BigDecimal;
import java.util.List;

public interface PagoService {
    List<Pago> listarTodos();
    Pago buscarPorId(Long id);
    List<Pago> listarPorMatricula(Long matriculaId);

    /** Marca un pago como PAGADO con la fecha actual y el método indicado. */
    Pago registrarPago(Long pagoId, String metodo);

    /** Suma los montos de los pagos que están en el estado indicado. */
    BigDecimal totalPorEstado(List<Pago> pagos, String estado);
}