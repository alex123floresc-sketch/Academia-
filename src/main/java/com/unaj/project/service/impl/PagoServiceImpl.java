package com.unaj.project.service.impl;

import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Pago;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.service.PagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    public List<Pago> listarTodos() {
        return pagoRepository.findAllConAlumno();
    }

    @Override
    public Page<Pago> buscarPagina(String q, Pageable pageable) {
        return pagoRepository.buscar(q, pageable);
    }

    @Override
    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado (id " + id + ")."));
    }

    @Override
    public List<Pago> listarPorMatricula(Long matriculaId) {
        return pagoRepository.findByMatriculaId(matriculaId);
    }

    @Override
    @Transactional
    public Pago registrarPago(Long pagoId, String metodo) {
        Pago pago = buscarPorId(pagoId);
        if ("PAGADO".equals(pago.getEstado())) {
            throw new IllegalStateException("Este pago ya fue registrado.");
        }
        pago.setEstado("PAGADO");
        pago.setFechaPago(LocalDateTime.now());
        pago.setMetodo(metodo);
        return pagoRepository.save(pago);
    }

    // ----- Lógica movida desde PagoController (Fase 3D) -----

    @Override
    public BigDecimal totalPorEstado(List<Pago> pagos, String estado) {
        return pagos.stream()
                .filter(p -> estado.equals(p.getEstado()))
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public int marcarVencidos() {
        return pagoRepository.marcarVencidos(LocalDate.now());
    }
}