package com.unaj.project.service.impl;

import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Abono;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Pago;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.AbonoRepository;
import com.unaj.project.repository.PagoRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.PagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final AbonoRepository abonoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagoServiceImpl(PagoRepository pagoRepository, AbonoRepository abonoRepository,
                           UsuarioRepository usuarioRepository) {
        this.pagoRepository = pagoRepository;
        this.abonoRepository = abonoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Pago> listarTodos() {
        return pagoRepository.findAllConAlumno();
    }

    @Override
    public Page<Alumno> buscarAlumnosConPagos(String q, Pageable pageable) {
        return pagoRepository.buscarAlumnosConPagos(q, pageable);
    }

    @Override
    public Map<Long, List<Pago>> agruparPorAlumno(List<Long> alumnoIds) {
        if (alumnoIds == null || alumnoIds.isEmpty()) {
            return Map.of();
        }
        return pagoRepository.buscarPorAlumnoIds(alumnoIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMatricula().getEstudiante().getId(),
                        LinkedHashMap::new, Collectors.toList()));
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
    public List<Abono> listarAbonos(Long pagoId) {
        return abonoRepository.findByPagoIdOrderByFechaDesc(pagoId);
    }

    @Override
    @Transactional
    public Pago registrarAbono(Long pagoId, BigDecimal monto, String metodo, String username) {
        Pago pago = buscarPorId(pagoId);

        BigDecimal saldo = pago.getSaldo();
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Este pago ya está saldado.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a 0.");
        }
        if (monto.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El abono no puede superar el saldo pendiente (S/ " + saldo + ").");
        }

        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        Abono abono = new Abono();
        abono.setPago(pago);
        abono.setMonto(monto);
        abono.setFecha(LocalDateTime.now());
        abono.setMetodo(metodo);
        abono.setRegistradoPor(registradoPor);
        abonoRepository.save(abono);

        pago.setMontoPagado(pago.getMontoPagado().add(monto));
        pago.setMetodo(metodo);
        if (pago.getMontoPagado().compareTo(pago.getMonto()) >= 0) {
            pago.setEstado("PAGADO");
            pago.setFechaPago(LocalDateTime.now());
        } else {
            pago.setEstado("PARCIAL");
        }

        return pagoRepository.save(pago);
    }


    @Override
    public BigDecimal totalPorEstado(List<Pago> pagos, String estado) {
        return pagos.stream()
                .filter(p -> estado.equals(p.getEstado()))
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal totalCobrado(List<Pago> pagos) {
        return pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal totalSaldo(List<Pago> pagos, String estado) {
        return pagos.stream()
                .filter(p -> estado.equals(p.getEstado()))
                .map(Pago::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public int marcarVencidos() {
        return pagoRepository.marcarVencidos(LocalDate.now());
    }
}
