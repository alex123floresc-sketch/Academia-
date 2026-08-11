package com.unaj.project.service.impl;

import com.unaj.project.dto.ProfesorForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.ProfesorRepository;
import com.unaj.project.service.ProfesorService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ProfesorServiceImpl implements ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final RegistroActividadService registroActividadService;

    public ProfesorServiceImpl(ProfesorRepository profesorRepository, RegistroActividadService registroActividadService) {
        this.profesorRepository = profesorRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Profesor> listarTodos() {
        return profesorRepository.findByEliminadoFalse();
    }

    @Override
    public List<Profesor> listarDestacados() {
        return profesorRepository.findByDestacadoWebTrueAndEliminadoFalse();
    }

    @Override
    public Page<Profesor> buscarPagina(String q, Pageable pageable) {
        return profesorRepository.buscar(q, pageable);
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
        boolean esNuevo = (form.getId() == null);
        Profesor profesor = !esNuevo ? buscarPorId(form.getId()) : new Profesor();

        boolean emailDuplicado = (form.getId() != null)
                ? profesorRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), form.getId())
                : profesorRepository.existsByEmailIgnoreCase(form.getEmail());
        if (emailDuplicado) {
            throw new IllegalArgumentException("Ya existe un profesor registrado con ese correo.");
        }

        profesor.setNombre(form.getNombre());
        profesor.setApellido(form.getApellido());
        profesor.setEmail(form.getEmail());
        profesor.setEspecialidad(form.getEspecialidad());
        profesor.setTarifaHora(form.getTarifaHora());
        profesor.setNiveles(form.getNiveles() != null ? form.getNiveles() : new java.util.LinkedHashSet<>());
        profesor.setDestacadoWeb(form.isDestacadoWeb());

        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                profesor.setFoto(foto.getBytes());
                profesor.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        Profesor guardado = profesorRepository.save(profesor);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Profesores",
                guardado.getId(), (esNuevo ? "Registró al profesor " : "Editó al profesor ")
                        + guardado.getNombre() + " " + guardado.getApellido());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        profesorRepository.findById(id).ifPresent(p -> {
            p.setEliminado(true);
            profesorRepository.save(p);
            registroActividadService.registrar(TipoAccion.ELIMINAR, "Profesores",
                    p.getId(), "Eliminó al profesor " + p.getNombre() + " " + p.getApellido());
        });
    }

    private ProfesorForm aForm(Profesor p) {
        ProfesorForm form = new ProfesorForm();
        form.setId(p.getId());
        form.setNombre(p.getNombre());
        form.setApellido(p.getApellido());
        form.setEmail(p.getEmail());
        form.setEspecialidad(p.getEspecialidad());
        form.setTarifaHora(p.getTarifaHora());
        form.setNiveles(new java.util.LinkedHashSet<>(p.getNiveles()));
        form.setDestacadoWeb(p.isDestacadoWeb());
        return form;
    }
}