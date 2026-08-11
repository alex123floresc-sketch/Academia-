package com.unaj.project.service.impl;

import com.unaj.project.model.PagoProfesor;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.PagoProfesorRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.PagoProfesorService;
import com.unaj.project.service.ProfesorService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagoProfesorServiceImpl implements PagoProfesorService {

    private final PagoProfesorRepository pagoProfesorRepository;
    private final ProfesorService profesorService;
    private final UsuarioRepository usuarioRepository;
    private final RegistroActividadService registroActividadService;
    private final ConfiguracionService configuracionService;

    public PagoProfesorServiceImpl(PagoProfesorRepository pagoProfesorRepository,
                                   ProfesorService profesorService,
                                   UsuarioRepository usuarioRepository,
                                   RegistroActividadService registroActividadService,
                                   ConfiguracionService configuracionService) {
        this.pagoProfesorRepository = pagoProfesorRepository;
        this.profesorService = profesorService;
        this.usuarioRepository = usuarioRepository;
        this.registroActividadService = registroActividadService;
        this.configuracionService = configuracionService;
    }

    @Override
    @Transactional
    public PagoProfesor registrar(Long profesorId, String tipoPeriodo, LocalDate periodoInicio, LocalDate periodoFin,
                                  BigDecimal horasPagadas, BigDecimal monto, LocalDate fechaPago, String metodo,
                                  String observaciones, String username) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");
        }
        if (!periodoFin.isAfter(periodoInicio) && !periodoFin.isEqual(periodoInicio)) {
            throw new IllegalArgumentException("El período de pago no es válido.");
        }

        Profesor profesor = profesorService.buscarPorId(profesorId);
        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        PagoProfesor pago = new PagoProfesor();
        pago.setProfesor(profesor);
        pago.setTipoPeriodo(tipoPeriodo);
        pago.setPeriodoInicio(periodoInicio);
        pago.setPeriodoFin(periodoFin);
        pago.setHorasPagadas(horasPagadas != null ? horasPagadas : BigDecimal.ZERO);
        pago.setMonto(monto);
        pago.setFechaPago(fechaPago != null ? fechaPago : LocalDate.now());
        pago.setMetodo(metodo);
        pago.setObservaciones(observaciones);
        pago.setRegistradoPor(registradoPor);
        PagoProfesor guardado = pagoProfesorRepository.save(pago);
        registroActividadService.registrar(username, TipoAccion.CREAR, "Horas docentes", guardado.getId(),
                "Registró un pago de " + configuracionService.obtener().getSimboloMoneda() + " " + monto + " a " + profesor.getNombre() + " " + profesor.getApellido());
        return guardado;
    }

    @Override
    public List<PagoProfesor> listarPorProfesor(Long profesorId) {
        return pagoProfesorRepository.findByProfesorIdOrderByFechaPagoDesc(profesorId);
    }
}
