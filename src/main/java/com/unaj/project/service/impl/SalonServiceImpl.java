package com.unaj.project.service.impl;

import com.unaj.project.dto.SalonForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Salon;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.SalonRepository;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.SalonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalonServiceImpl implements SalonService {

    private final SalonRepository salonRepository;
    private final AlumnoRepository alumnoRepository;
    private final RegistroActividadService registroActividadService;

    public SalonServiceImpl(SalonRepository salonRepository, AlumnoRepository alumnoRepository,
                            RegistroActividadService registroActividadService) {
        this.salonRepository = salonRepository;
        this.alumnoRepository = alumnoRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Salon> listarTodos() {
        return salonRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<Salon> listarActivos() {
        return salonRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public Salon buscarPorId(Long id) {
        return salonRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Salón no encontrado (id " + id + ")."));
    }

    @Override
    public SalonForm buscarFormPorId(Long id) {
        Salon salon = buscarPorId(id);
        SalonForm form = new SalonForm();
        form.setId(salon.getId());
        form.setNombre(salon.getNombre());
        form.setCapacidad(salon.getCapacidad());
        form.setActivo(salon.isActivo());
        return form;
    }

    @Override
    @Transactional
    public void guardar(SalonForm form) {
        boolean esNuevo = (form.getId() == null);

        boolean nombreDuplicado = esNuevo
                ? salonRepository.existsByNombreIgnoreCase(form.getNombre())
                : salonRepository.existsByNombreIgnoreCaseAndIdNot(form.getNombre(), form.getId());
        if (nombreDuplicado) {
            throw new IllegalArgumentException("Ya existe un salón con ese nombre.");
        }

        Salon salon = !esNuevo ? buscarPorId(form.getId()) : new Salon();
        salon.setNombre(form.getNombre());
        salon.setCapacidad(form.getCapacidad());
        salon.setActivo(form.isActivo());
        Salon guardado = salonRepository.save(salon);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Salones",
                guardado.getId(), (esNuevo ? "Creó el salón " : "Editó el salón ") + "\"" + guardado.getNombre() + "\"");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Salon salon = buscarPorId(id);
        long alumnosAsignados = alumnoRepository.countBySalonIdAndEliminadoFalse(id);
        if (alumnosAsignados > 0) {
            throw new IllegalArgumentException("No se puede eliminar el salón \"" + salon.getNombre() + "\": tiene "
                    + alumnosAsignados + " alumno" + (alumnosAsignados == 1 ? "" : "s") + " asignado"
                    + (alumnosAsignados == 1 ? "" : "s") + ". Reasígnalos primero.");
        }
        salonRepository.delete(salon);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Salones", id,
                "Eliminó el salón \"" + salon.getNombre() + "\"");
    }
}
