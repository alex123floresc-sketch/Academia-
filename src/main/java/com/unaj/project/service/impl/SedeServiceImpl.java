package com.unaj.project.service.impl;

import com.unaj.project.dto.SedeForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Sede;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.SedeRepository;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.SedeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;
    private final RegistroActividadService registroActividadService;

    public SedeServiceImpl(SedeRepository sedeRepository, RegistroActividadService registroActividadService) {
        this.sedeRepository = sedeRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Sede> listarTodas() {
        return sedeRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<Sede> listarActivas() {
        return sedeRepository.findByActivaTrueOrderByNombreAsc();
    }

    @Override
    public Sede buscarPorId(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada (id " + id + ")."));
    }

    @Override
    public SedeForm buscarFormPorId(Long id) {
        Sede sede = buscarPorId(id);
        SedeForm form = new SedeForm();
        form.setId(sede.getId());
        form.setNombre(sede.getNombre());
        form.setDireccion(sede.getDireccion());
        form.setTelefono(sede.getTelefono());
        form.setHorario(sede.getHorario());
        form.setActiva(sede.isActiva());
        return form;
    }

    @Override
    @Transactional
    public void guardar(SedeForm form) {
        boolean esNueva = (form.getId() == null);
        Sede sede = !esNueva ? buscarPorId(form.getId()) : new Sede();

        sede.setNombre(form.getNombre());
        sede.setDireccion(form.getDireccion());
        sede.setTelefono(blankToNull(form.getTelefono()));
        sede.setHorario(blankToNull(form.getHorario()));
        sede.setActiva(form.isActiva());

        if (form.isQuitarFoto()) {
            sede.setFoto(null);
            sede.setFotoContentType(null);
        }
        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                sede.setFoto(foto.getBytes());
                sede.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        Sede guardada = sedeRepository.save(sede);
        registroActividadService.registrar(esNueva ? TipoAccion.CREAR : TipoAccion.EDITAR, "Sedes",
                guardada.getId(), (esNueva ? "Registró la sede " : "Editó la sede ") + guardada.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Sede sede = buscarPorId(id);
        sedeRepository.delete(sede);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Sedes", id, "Eliminó la sede " + sede.getNombre());
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
