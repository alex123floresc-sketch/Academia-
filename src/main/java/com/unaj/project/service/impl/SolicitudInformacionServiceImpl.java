package com.unaj.project.service.impl;

import com.unaj.project.dto.SolicitudInformacionForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.SolicitudInformacion;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.SolicitudInformacionRepository;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.SolicitudInformacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudInformacionServiceImpl implements SolicitudInformacionService {

    private final SolicitudInformacionRepository solicitudInformacionRepository;
    private final RegistroActividadService registroActividadService;

    public SolicitudInformacionServiceImpl(SolicitudInformacionRepository solicitudInformacionRepository,
                                           RegistroActividadService registroActividadService) {
        this.solicitudInformacionRepository = solicitudInformacionRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    @Transactional
    public void registrar(SolicitudInformacionForm form) {
        SolicitudInformacion s = new SolicitudInformacion();
        s.setNombre(form.getNombre());
        s.setTelefono(form.getTelefono());
        s.setCorreo(blankToNull(form.getCorreo()));
        s.setNivelInteres(form.getNivelInteres());
        s.setMensaje(blankToNull(form.getMensaje()));
        s.setFecha(LocalDateTime.now());
        s.setAtendida(false);
        solicitudInformacionRepository.save(s);
    }

    @Override
    public List<SolicitudInformacion> listarTodas() {
        return solicitudInformacionRepository.findAllByOrderByFechaDesc();
    }

    @Override
    public long contarNoAtendidas() {
        return solicitudInformacionRepository.countByAtendidaFalse();
    }

    @Override
    @Transactional
    public void marcarAtendida(Long id, boolean atendida) {
        SolicitudInformacion s = solicitudInformacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada (id " + id + ")."));
        s.setAtendida(atendida);
        solicitudInformacionRepository.save(s);
        registroActividadService.registrar(TipoAccion.EDITAR, "Solicitudes de información", id,
                (atendida ? "Marcó como atendida" : "Marcó como pendiente") + " la solicitud de " + s.getNombre());
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
