package com.unaj.project.service.impl;

import com.unaj.project.dto.CicloForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Ciclo;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.service.CicloService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CicloServiceImpl implements CicloService {

    private final CicloRepository cicloRepository;

    public CicloServiceImpl(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
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
        Ciclo ciclo = (form.getId() != null) ? buscarPorId(form.getId()) : new Ciclo();
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
        cicloRepository.save(ciclo);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        cicloRepository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            cicloRepository.save(c);
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