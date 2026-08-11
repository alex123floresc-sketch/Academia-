package com.unaj.project.service.impl;

import com.unaj.project.dto.PreguntaFrecuenteForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.PreguntaFrecuente;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.PreguntaFrecuenteRepository;
import com.unaj.project.service.PreguntaFrecuenteService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreguntaFrecuenteServiceImpl implements PreguntaFrecuenteService {

    private final PreguntaFrecuenteRepository preguntaFrecuenteRepository;
    private final RegistroActividadService registroActividadService;

    public PreguntaFrecuenteServiceImpl(PreguntaFrecuenteRepository preguntaFrecuenteRepository,
                                        RegistroActividadService registroActividadService) {
        this.preguntaFrecuenteRepository = preguntaFrecuenteRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<PreguntaFrecuente> listarTodas() {
        return preguntaFrecuenteRepository.findAllByOrderByOrdenAscIdAsc();
    }

    @Override
    public List<PreguntaFrecuente> listarActivas() {
        return preguntaFrecuenteRepository.findByActivaTrueOrderByOrdenAscIdAsc();
    }

    @Override
    public PreguntaFrecuente buscarPorId(Long id) {
        return preguntaFrecuenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta frecuente no encontrada (id " + id + ")."));
    }

    @Override
    public PreguntaFrecuenteForm buscarFormPorId(Long id) {
        PreguntaFrecuente p = buscarPorId(id);
        PreguntaFrecuenteForm form = new PreguntaFrecuenteForm();
        form.setId(p.getId());
        form.setPregunta(p.getPregunta());
        form.setRespuesta(p.getRespuesta());
        form.setOrden(p.getOrden());
        form.setActiva(p.isActiva());
        return form;
    }

    @Override
    @Transactional
    public void guardar(PreguntaFrecuenteForm form) {
        boolean esNueva = (form.getId() == null);
        PreguntaFrecuente p = !esNueva ? buscarPorId(form.getId()) : new PreguntaFrecuente();
        p.setPregunta(form.getPregunta());
        p.setRespuesta(form.getRespuesta());
        p.setOrden(form.getOrden());
        p.setActiva(form.isActiva());
        PreguntaFrecuente guardada = preguntaFrecuenteRepository.save(p);
        registroActividadService.registrar(esNueva ? TipoAccion.CREAR : TipoAccion.EDITAR, "Preguntas frecuentes",
                guardada.getId(), (esNueva ? "Creó la pregunta " : "Editó la pregunta ") + "\"" + guardada.getPregunta() + "\"");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PreguntaFrecuente p = buscarPorId(id);
        preguntaFrecuenteRepository.delete(p);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Preguntas frecuentes", id,
                "Eliminó la pregunta \"" + p.getPregunta() + "\"");
    }
}
