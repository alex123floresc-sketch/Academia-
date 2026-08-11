package com.unaj.project.service.impl;

import com.unaj.project.dto.FotoGaleriaForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.FotoGaleria;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.FotoGaleriaRepository;
import com.unaj.project.service.FotoGaleriaService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class FotoGaleriaServiceImpl implements FotoGaleriaService {

    private final FotoGaleriaRepository fotoGaleriaRepository;
    private final RegistroActividadService registroActividadService;

    public FotoGaleriaServiceImpl(FotoGaleriaRepository fotoGaleriaRepository, RegistroActividadService registroActividadService) {
        this.fotoGaleriaRepository = fotoGaleriaRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<FotoGaleria> listarTodas() {
        return fotoGaleriaRepository.findAllByOrderByOrdenAscIdAsc();
    }

    @Override
    public List<FotoGaleria> listarActivas() {
        return fotoGaleriaRepository.findByActivaTrueOrderByOrdenAscIdAsc();
    }

    @Override
    public FotoGaleria buscarPorId(Long id) {
        return fotoGaleriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Foto no encontrada (id " + id + ")."));
    }

    @Override
    public FotoGaleriaForm buscarFormPorId(Long id) {
        FotoGaleria f = buscarPorId(id);
        FotoGaleriaForm form = new FotoGaleriaForm();
        form.setId(f.getId());
        form.setDescripcion(f.getDescripcion());
        form.setOrden(f.getOrden());
        form.setActiva(f.isActiva());
        return form;
    }

    @Override
    @Transactional
    public void guardar(FotoGaleriaForm form) {
        boolean esNueva = (form.getId() == null);
        MultipartFile imagen = form.getImagen();
        if (esNueva && (imagen == null || imagen.isEmpty())) {
            throw new IllegalArgumentException("Debes seleccionar una imagen.");
        }

        FotoGaleria f = !esNueva ? buscarPorId(form.getId()) : new FotoGaleria();
        f.setDescripcion(blankToNull(form.getDescripcion()));
        f.setOrden(form.getOrden());
        f.setActiva(form.isActiva());

        if (imagen != null && !imagen.isEmpty()) {
            try {
                f.setImagen(imagen.getBytes());
                f.setImagenContentType(imagen.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la imagen enviada.", e);
            }
        }

        FotoGaleria guardada = fotoGaleriaRepository.save(f);
        registroActividadService.registrar(esNueva ? TipoAccion.CREAR : TipoAccion.EDITAR, "Galería de fotos",
                guardada.getId(), esNueva ? "Agregó una foto a la galería" : "Editó una foto de la galería");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        FotoGaleria f = buscarPorId(id);
        fotoGaleriaRepository.delete(f);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Galería de fotos", id, "Eliminó una foto de la galería");
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
