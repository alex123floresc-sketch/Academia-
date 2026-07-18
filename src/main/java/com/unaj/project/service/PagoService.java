package com.unaj.project.service;

import com.unaj.project.model.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface PagoService {
    List<Pago> listarTodos();
    Page<Pago> buscarPagina(String q, Pageable pageable);
    Pago buscarPorId(Long id);
    List<Pago> listarPorMatricula(Long matriculaId);

    /** Marca un pago como PAGADO con la fecha actual y el método indicado. */
    Pago registrarPago(Long pagoId, String metodo);

    /** Suma los montos de los pagos que están en el estado indicado. */
    BigDecimal totalPorEstado(List<Pago> pagos, String estado);

    /** Marca como VENCIDO todo pago PENDIENTE cuya fecha de vencimiento ya pasó. Devuelve la cantidad actualizada. */
    int marcarVencidos();
}