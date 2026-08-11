package com.unaj.project.service.impl;

import com.unaj.project.dto.TestimonioForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.model.Testimonio;
import com.unaj.project.repository.TestimonioRepository;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.TestimonioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class TestimonioServiceImpl implements TestimonioService {

    private final TestimonioRepository testimonioRepository;
    private final RegistroActividadService registroActividadService;

    public TestimonioServiceImpl(TestimonioRepository testimonioRepository, RegistroActividadService registroActividadService) {
        this.testimonioRepository = testimonioRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Testimonio> listarTodos() {
        return testimonioRepository.findAllByOrderByOrdenAscIdAsc();
    }

    @Override
    public List<Testimonio> listarActivos() {
        return testimonioRepository.findByActivoTrueOrderByOrdenAscIdAsc();
    }

    @Override
    public Testimonio buscarPorId(Long id) {
        return testimonioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Testimonio no encontrado (id " + id + ")."));
    }

    @Override
    public TestimonioForm buscarFormPorId(Long id) {
        Testimonio t = buscarPorId(id);
        TestimonioForm form = new TestimonioForm();
        form.setId(t.getId());
        form.setNombre(t.getNombre());
        form.setRol(t.getRol());
        form.setComentario(t.getComentario());
        form.setOrden(t.getOrden());
        form.setActivo(t.isActivo());
        return form;
    }

    @Override
    @Transactional
    public void guardar(TestimonioForm form) {
        boolean esNuevo = (form.getId() == null);
        Testimonio t = !esNuevo ? buscarPorId(form.getId()) : new Testimonio();
        t.setNombre(form.getNombre());
        t.setRol(blankToNull(form.getRol()));
        t.setComentario(form.getComentario());
        t.setOrden(form.getOrden());
        t.setActivo(form.isActivo());

        if (form.isQuitarFoto()) {
            t.setFoto(null);
            t.setFotoContentType(null);
        }
        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                t.setFoto(foto.getBytes());
                t.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        Testimonio guardado = testimonioRepository.save(t);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Testimonios",
                guardado.getId(), (esNuevo ? "Creó el testimonio de " : "Editó el testimonio de ") + guardado.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Testimonio t = buscarPorId(id);
        testimonioRepository.delete(t);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Testimonios", id, "Eliminó el testimonio de " + t.getNombre());
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
