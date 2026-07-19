package com.unaj.project.service.impl;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Profesor;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.ProfesorRepository;
import com.unaj.project.service.CursoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;

    public CursoServiceImpl(CursoRepository cursoRepository, ProfesorRepository profesorRepository) {
        this.cursoRepository = cursoRepository;
        this.profesorRepository = profesorRepository;
    }

    @Override
    public List<Curso> listarTodos() {
        return cursoRepository.findAllConProfesor();
    }

    @Override
    public Page<Curso> buscarPagina(String q, Pageable pageable) {
        return cursoRepository.buscar(q, pageable);
    }

    @Override
    public Curso buscarPorId(Long id) {
        return cursoRepository.findByIdConProfesor(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado (id " + id + ")."));
    }

    @Override
    public CursoForm buscarFormPorId(Long id) {
        return aForm(buscarPorId(id));
    }

    @Override
    @Transactional
    public void guardar(CursoForm form) {
        Curso curso = (form.getId() != null) ? buscarPorId(form.getId()) : new Curso();
        curso.setCodigo(form.getCodigo());
        curso.setNombre(form.getNombre());
        curso.setHoras(form.getHoras());

        if (form.getProfesorId() != null) {
            Profesor profesor = profesorRepository.findById(form.getProfesorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado: " + form.getProfesorId()));
            curso.setProfesor(profesor);
        } else {
            curso.setProfesor(null);
        }
        cursoRepository.save(curso);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        cursoRepository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            cursoRepository.save(c);
        });
    }

    private CursoForm aForm(Curso c) {
        CursoForm form = new CursoForm();
        form.setId(c.getId());
        form.setCodigo(c.getCodigo());
        form.setNombre(c.getNombre());
        form.setHoras(c.getHoras());
        form.setProfesorId(c.getProfesor() != null ? c.getProfesor().getId() : null);
        return form;
    }
}