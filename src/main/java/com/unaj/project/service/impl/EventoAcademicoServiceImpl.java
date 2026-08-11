package com.unaj.project.service.impl;

import com.unaj.project.dto.EventoAcademicoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.EventoAcademico;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.EventoAcademicoRepository;
import com.unaj.project.service.EventoAcademicoService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoAcademicoServiceImpl implements EventoAcademicoService {

    private final EventoAcademicoRepository eventoAcademicoRepository;
    private final RegistroActividadService registroActividadService;

    public EventoAcademicoServiceImpl(EventoAcademicoRepository eventoAcademicoRepository, RegistroActividadService registroActividadService) {
        this.eventoAcademicoRepository = eventoAcademicoRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<EventoAcademico> listarTodos() {
        return eventoAcademicoRepository.findAllByOrderByFechaAsc();
    }

    @Override
    public List<EventoAcademico> listarProximos() {
        return eventoAcademicoRepository.findByActivoTrueAndFechaGreaterThanEqualOrderByFechaAsc(LocalDate.now());
    }

    @Override
    public EventoAcademico buscarPorId(Long id) {
        return eventoAcademicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado (id " + id + ")."));
    }

    @Override
    public EventoAcademicoForm buscarFormPorId(Long id) {
        EventoAcademico e = buscarPorId(id);
        EventoAcademicoForm form = new EventoAcademicoForm();
        form.setId(e.getId());
        form.setTitulo(e.getTitulo());
        form.setDescripcion(e.getDescripcion());
        form.setFecha(e.getFecha());
        form.setTipo(e.getTipo());
        form.setActivo(e.isActivo());
        return form;
    }

    @Override
    @Transactional
    public void guardar(EventoAcademicoForm form) {
        boolean esNuevo = (form.getId() == null);
        EventoAcademico e = !esNuevo ? buscarPorId(form.getId()) : new EventoAcademico();
        e.setTitulo(form.getTitulo());
        e.setDescripcion(blankToNull(form.getDescripcion()));
        e.setFecha(form.getFecha());
        e.setTipo(blankToNull(form.getTipo()));
        e.setActivo(form.isActivo());
        EventoAcademico guardado = eventoAcademicoRepository.save(e);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Calendario académico",
                guardado.getId(), (esNuevo ? "Creó el evento " : "Editó el evento ") + "\"" + guardado.getTitulo() + "\"");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        EventoAcademico e = buscarPorId(id);
        eventoAcademicoRepository.delete(e);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Calendario académico", id, "Eliminó el evento \"" + e.getTitulo() + "\"");
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
