package com.unaj.project.service.impl;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Ciclo;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.service.CicloService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CicloServiceImpl implements CicloService {

    private final CicloRepository cicloRepository;
    private final RegistroActividadService registroActividadService;

    public CicloServiceImpl(CicloRepository cicloRepository, RegistroActividadService registroActividadService) {
        this.cicloRepository = cicloRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Ciclo> listarTodos() {
        return cicloRepository.findByEliminadoFalse();
    }

    @Override
    public Page<Ciclo> buscarPagina(String q, Pageable pageable) {
        return cicloRepository.buscar(q, pageable);
    }

    @Override
    public Ciclo buscarPorId(Long id) {
        return cicloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ciclo no encontrado (id " + id + ")."));
    }

    @Override
    public CicloForm buscarFormPorId(Long id) {
        return aForm(buscarPorId(id));
    }

    @Override
    public Ciclo obtenerActivo() {
        return cicloRepository.findFirstByActivoTrueAndEliminadoFalse();
    }

    @Override
    @Transactional
    public void guardar(CicloForm form) {
        boolean esNuevo = (form.getId() == null);
        Ciclo ciclo = !esNuevo ? buscarPorId(form.getId()) : new Ciclo();
        ciclo.setNombre(form.getNombre());
        ciclo.setFechaInicio(form.getFechaInicio());
        ciclo.setFechaFin(form.getFechaFin());
        ciclo.setActivo(form.isActivo());

        if (ciclo.isActivo()) {
            Ciclo cicloActivoActual = cicloRepository.findFirstByActivoTrueAndEliminadoFalse();
            if (cicloActivoActual != null && !Objects.equals(cicloActivoActual.getId(), ciclo.getId())) {
                cicloActivoActual.setActivo(false);
            }
        }
        Ciclo guardado = cicloRepository.save(ciclo);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Ciclos",
                guardado.getId(), (esNuevo ? "Creó el ciclo " : "Editó el ciclo ") + guardado.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        cicloRepository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            cicloRepository.save(c);
            registroActividadService.registrar(TipoAccion.ELIMINAR, "Ciclos", c.getId(), "Eliminó el ciclo " + c.getNombre());
        });
    }

    private CicloForm aForm(Ciclo c) {
        CicloForm form = new CicloForm();
        form.setId(c.getId());
        form.setNombre(c.getNombre());
        form.setFechaInicio(c.getFechaInicio());
        form.setFechaFin(c.getFechaFin());
        form.setActivo(c.isActivo());
        return form;
    }
}