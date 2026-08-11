package com.unaj.project.service.impl;

import com.unaj.project.dto.LogroIngresoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.LogroIngreso;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.LogroIngresoRepository;
import com.unaj.project.service.LogroIngresoService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class LogroIngresoServiceImpl implements LogroIngresoService {

    private final LogroIngresoRepository logroIngresoRepository;
    private final RegistroActividadService registroActividadService;

    public LogroIngresoServiceImpl(LogroIngresoRepository logroIngresoRepository, RegistroActividadService registroActividadService) {
        this.logroIngresoRepository = logroIngresoRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<LogroIngreso> listarTodos() {
        return logroIngresoRepository.findAllByOrderByOrdenAscIdAsc();
    }

    @Override
    public List<LogroIngreso> listarActivos() {
        return logroIngresoRepository.findByActivoTrueOrderByOrdenAscIdAsc();
    }

    @Override
    public LogroIngreso buscarPorId(Long id) {
        return logroIngresoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Resultado de ingreso no encontrado (id " + id + ")."));
    }

    @Override
    public LogroIngresoForm buscarFormPorId(Long id) {
        LogroIngreso l = buscarPorId(id);
        LogroIngresoForm form = new LogroIngresoForm();
        form.setId(l.getId());
        form.setNombreAlumno(l.getNombreAlumno());
        form.setUniversidad(l.getUniversidad());
        form.setCarrera(l.getCarrera());
        form.setAnioIngreso(l.getAnioIngreso());
        form.setOrden(l.getOrden());
        form.setActivo(l.isActivo());
        return form;
    }

    @Override
    @Transactional
    public void guardar(LogroIngresoForm form) {
        boolean esNuevo = (form.getId() == null);
        LogroIngreso l = !esNuevo ? buscarPorId(form.getId()) : new LogroIngreso();
        l.setNombreAlumno(form.getNombreAlumno());
        l.setUniversidad(form.getUniversidad());
        l.setCarrera(blankToNull(form.getCarrera()));
        l.setAnioIngreso(form.getAnioIngreso());
        l.setOrden(form.getOrden());
        l.setActivo(form.isActivo());

        if (form.isQuitarFoto()) {
            l.setFoto(null);
            l.setFotoContentType(null);
        }
        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                l.setFoto(foto.getBytes());
                l.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        LogroIngreso guardado = logroIngresoRepository.save(l);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Resultados de ingreso",
                guardado.getId(), (esNuevo ? "Creó el resultado de ingreso de " : "Editó el resultado de ingreso de ") + guardado.getNombreAlumno());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        LogroIngreso l = buscarPorId(id);
        logroIngresoRepository.delete(l);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Resultados de ingreso", id,
                "Eliminó el resultado de ingreso de " + l.getNombreAlumno());
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
