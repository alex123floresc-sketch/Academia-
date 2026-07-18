package com.unaj.project.service.impl;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Profesor;
import com.unaj.project.repository.ProfesorRepository;
import com.unaj.project.service.ProfesorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfesorServiceImpl implements ProfesorService {

    private final ProfesorRepository profesorRepository;

    public ProfesorServiceImpl(ProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    @Override
    public List<Profesor> listarTodos() {
        return profesorRepository.findByEliminadoFalse();
    }

    @Override
    public Profesor buscarPorId(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado (id " + id + ")."));
    }

    @Override
    public ProfesorForm buscarFormPorId(Long id) {
        return aForm(buscarPorId(id));
    }

    @Override
    @Transactional
    public void guardar(ProfesorForm form) {
        Profesor profesor = (form.getId() != null) ? buscarPorId(form.getId()) : new Profesor();
        profesor.setNombre(form.getNombre());
        profesor.setApellido(form.getApellido());
        profesor.setEmail(form.getEmail());
        profesor.setEspecialidad(form.getEspecialidad());
        profesorRepository.save(profesor);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        profesorRepository.findById(id).ifPresent(p -> {
            p.setEliminado(true);
            profesorRepository.save(p);
        });
    }

    private ProfesorForm aForm(Profesor p) {
        ProfesorForm form = new ProfesorForm();
        form.setId(p.getId());
        form.setNombre(p.getNombre());
        form.setApellido(p.getApellido());
        form.setEmail(p.getEmail());
        form.setEspecialidad(p.getEspecialidad());
        return form;
    }
}