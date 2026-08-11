package com.unaj.project.service.impl;

import com.unaj.project.dto.PasoAdmisionForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.PasoAdmision;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.PasoAdmisionRepository;
import com.unaj.project.service.PasoAdmisionService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasoAdmisionServiceImpl implements PasoAdmisionService {

    private final PasoAdmisionRepository pasoAdmisionRepository;
    private final RegistroActividadService registroActividadService;

    public PasoAdmisionServiceImpl(PasoAdmisionRepository pasoAdmisionRepository,
                                   RegistroActividadService registroActividadService) {
        this.pasoAdmisionRepository = pasoAdmisionRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<PasoAdmision> listarTodos() {
        return pasoAdmisionRepository.findAllByOrderByOrdenAscIdAsc();
    }

    @Override
    public List<PasoAdmision> listarActivos() {
        return pasoAdmisionRepository.findByActivoTrueOrderByOrdenAscIdAsc();
    }

    @Override
    public PasoAdmision buscarPorId(Long id) {
        return pasoAdmisionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paso de admisión no encontrado (id " + id + ")."));
    }

    @Override
    public PasoAdmisionForm buscarFormPorId(Long id) {
        PasoAdmision p = buscarPorId(id);
        PasoAdmisionForm form = new PasoAdmisionForm();
        form.setId(p.getId());
        form.setTitulo(p.getTitulo());
        form.setDescripcion(p.getDescripcion());
        form.setOrden(p.getOrden());
        form.setActivo(p.isActivo());
        return form;
    }

    @Override
    @Transactional
    public void guardar(PasoAdmisionForm form) {
        boolean esNuevo = (form.getId() == null);
        PasoAdmision p = !esNuevo ? buscarPorId(form.getId()) : new PasoAdmision();
        p.setTitulo(form.getTitulo());
        p.setDescripcion(form.getDescripcion());
        p.setOrden(form.getOrden());
        p.setActivo(form.isActivo());
        PasoAdmision guardado = pasoAdmisionRepository.save(p);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Proceso de admisión",
                guardado.getId(), (esNuevo ? "Creó el paso " : "Editó el paso ") + "\"" + guardado.getTitulo() + "\"");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PasoAdmision p = buscarPorId(id);
        pasoAdmisionRepository.delete(p);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Proceso de admisión", id,
                "Eliminó el paso \"" + p.getTitulo() + "\"");
    }
}
