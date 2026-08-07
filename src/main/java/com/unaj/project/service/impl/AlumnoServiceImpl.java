package com.unaj.project.service.impl;

import com.unaj.project.dto.AlumnoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Areas;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.service.AlumnoService;
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

    public AlumnoServiceImpl(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public List<Alumno> listarTodos() {
        return alumnoRepository.findByEliminadoFalse();
    }

    @Override
    public Map<String, Long> contarPorArea() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String area : Areas.TODAS) {
            conteo.put(area, 0L);
        }
        conteo.put(SIN_AREA, 0L);
        for (Alumno alumno : listarTodos()) {
            String area = Areas.TODAS.stream()
                    .filter(a -> a.equalsIgnoreCase(alumno.getArea()))
                    .findFirst()
                    .orElse(SIN_AREA);
            conteo.merge(area, 1L, Long::sum);
        }
        return conteo;
    }

    @Override
    public Page<Alumno> buscarPagina(String q, Pageable pageable) {
        return alumnoRepository.buscar(q, null, pageable);
    }

    @Override
    public Page<Alumno> buscarPagina(String q, String area, Pageable pageable) {
        return alumnoRepository.buscar(q, area, pageable);
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
        if (form.getId() != null) {
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

        MultipartFile foto = form.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                alumno.setFoto(foto.getBytes());
                alumno.setFotoContentType(foto.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la fotografía enviada.", e);
            }
        }

        return alumnoRepository.save(alumno);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        alumnoRepository.findById(id).ifPresent(a -> {
            a.setEliminado(true);
            alumnoRepository.save(a);
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
        return form;
    }
}