package com.unaj.project.service.impl;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Salon;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.SalonRepository;
import com.unaj.project.service.AlumnoService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlumnoServiceImpl implements AlumnoService {

    private static final String SIN_AREA = "Sin área";

    private final AlumnoRepository alumnoRepository;
    private final SalonRepository salonRepository;
    private final RegistroActividadService registroActividadService;

    public AlumnoServiceImpl(AlumnoRepository alumnoRepository, SalonRepository salonRepository,
                             RegistroActividadService registroActividadService) {
        this.alumnoRepository = alumnoRepository;
        this.salonRepository = salonRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Alumno> listarTodos() {
        return alumnoRepository.findByEliminadoFalse();
    }

    @Override
    public Map<String, Long> contarPorArea(Nivel nivel) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String area : Areas.paraNivel(nivel)) {
            conteo.put(area, 0L);
        }
        conteo.put(SIN_AREA, 0L);
        for (Alumno alumno : listarTodos()) {
            if (alumno.getNivel() != nivel) continue;
            String area = Areas.paraNivel(nivel).stream()
                    .filter(a -> a.equalsIgnoreCase(alumno.getArea()))
                    .findFirst()
                    .orElse(SIN_AREA);
            conteo.merge(area, 1L, Long::sum);
        }
        return conteo;
    }

    @Override
    public Map<Nivel, Long> contarPorNivel() {
        Map<Nivel, Long> conteo = new LinkedHashMap<>();
        for (Nivel nivel : Nivel.values()) {
            conteo.put(nivel, 0L);
        }
        for (Alumno alumno : listarTodos()) {
            if (alumno.getNivel() != null) {
                conteo.merge(alumno.getNivel(), 1L, Long::sum);
            }
        }
        return conteo;
    }

    @Override
    public Page<Alumno> buscarPagina(String q, Pageable pageable) {
        return alumnoRepository.buscar(q, null, null, null, pageable);
    }

    @Override
    public Page<Alumno> buscarPagina(String q, Nivel nivel, String area, Pageable pageable) {
        return alumnoRepository.buscar(q, nivel, area, null, pageable);
    }

    @Override
    public Page<Alumno> buscarPagina(String q, Nivel nivel, String area, Long salonId, Pageable pageable) {
        return alumnoRepository.buscar(q, nivel, area, salonId, pageable);
    }

    @Override
    public Alumno buscarPorId(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado (id " + id + ")."));
    }

    @Override
    public AlumnoForm buscarFormPorId(Long id) {
        Alumno alumno = buscarPorId(id);
        return aForm(alumno);
    }

    @Override
    @Transactional
    public Alumno guardar(AlumnoForm form) {
        Alumno alumno;
        boolean esNuevo = (form.getId() == null);
        if (!esNuevo) {
            alumno = buscarPorId(form.getId());
        } else {
            alumno = new Alumno();
        }

        String email = (form.getEmail() != null && !form.getEmail().isBlank()) ? form.getEmail() : null;
        if (email != null) {
            boolean emailDuplicado = (form.getId() != null)
                    ? alumnoRepository.existsByEmailIgnoreCaseAndIdNot(email, form.getId())
                    : alumnoRepository.existsByEmailIgnoreCase(email);
            if (emailDuplicado) {
                throw new IllegalArgumentException("Ya existe un alumno registrado con ese correo.");
            }
        }

        boolean dniDuplicado = (form.getId() != null)
                ? alumnoRepository.existsByDniAndIdNot(form.getDni(), form.getId())
                : alumnoRepository.existsByDni(form.getDni());
        if (dniDuplicado) {
            throw new IllegalArgumentException("Ya existe un alumno registrado con ese DNI.");
        }

        alumno.setNombre(form.getNombre());
        alumno.setApellido(form.getApellido());
        alumno.setEmail(email);
        alumno.setCelular(form.getCelular());
        alumno.setDni(form.getDni());
        alumno.setNombrePadre(form.getNombrePadre());
        alumno.setTelefonoPadre(form.getTelefonoPadre());
        alumno.setArea(form.getArea());
        alumno.setNivel(form.getNivel());

        if (form.getNivel() == Nivel.PREUNIVERSITARIO && form.getSalonId() != null) {
            Salon salon = salonRepository.findById(form.getSalonId())
                    .orElseThrow(() -> new IllegalArgumentException("El salón seleccionado no existe."));
            alumno.setSalon(salon);
        } else {
            alumno.setSalon(null);
        }

        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                alumno.setFoto(foto.getBytes());
                alumno.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        Alumno guardado = alumnoRepository.save(alumno);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Alumnos",
                guardado.getId(), (esNuevo ? "Registró al alumno " : "Editó al alumno ")
                        + guardado.getNombre() + " " + guardado.getApellido());
        return guardado;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        alumnoRepository.findById(id).ifPresent(a -> {
            a.setEliminado(true);
            alumnoRepository.save(a);
            registroActividadService.registrar(TipoAccion.ELIMINAR, "Alumnos",
                    a.getId(), "Eliminó al alumno " + a.getNombre() + " " + a.getApellido());
        });
    }

    private AlumnoForm aForm(Alumno alumno) {
        AlumnoForm form = new AlumnoForm();
        form.setId(alumno.getId());
        form.setNombre(alumno.getNombre());
        form.setApellido(alumno.getApellido());
        form.setEmail(alumno.getEmail());
        form.setCelular(alumno.getCelular());
        form.setDni(alumno.getDni());
        form.setNombrePadre(alumno.getNombrePadre());
        form.setTelefonoPadre(alumno.getTelefonoPadre());
        form.setArea(alumno.getArea());
        form.setNivel(alumno.getNivel());
        form.setSalonId(alumno.getSalon() != null ? alumno.getSalon().getId() : null);
        return form;
    }
}
