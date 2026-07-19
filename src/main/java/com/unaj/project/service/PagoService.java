package com.unaj.project.service;

import com.unaj.project.model.Abono;
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

    /** Abonos registrados para un pago, del más reciente al más antiguo. */
    List<Abono> listarAbonos(Long pagoId);

    /**
     * Registra un abono (pago total o parcial) sobre un Pago. Si el abono cubre el saldo
     * restante, el Pago queda PAGADO; si no, queda PARCIAL.
     */
    Pago registrarAbono(Long pagoId, BigDecimal monto, String metodo, String username);

    /** Suma los montos de los pagos que están en el estado indicado. */
    BigDecimal totalPorEstado(List<Pago> pagos, String estado);

    /** Suma el dinero realmente cobrado (montoPagado) de una lista de pagos. */
    BigDecimal totalCobrado(List<Pago> pagos);

    /** Suma el saldo pendiente (monto - montoPagado) de los pagos en el estado indicado. */
    BigDecimal totalSaldo(List<Pago> pagos, String estado);

    /** Marca como VENCIDO todo pago PENDIENTE o PARCIAL cuya fecha de vencimiento ya pasó. Devuelve la cantidad actualizada. */
    int marcarVencidos();
}
