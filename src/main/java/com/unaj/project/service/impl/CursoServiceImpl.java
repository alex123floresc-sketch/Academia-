package com.unaj.project.service.impl;

import com.unaj.project.dto.CursoForm;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.Areas;
import com.unaj.project.model.Curso;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.ProfesorRepository;
import com.unaj.project.service.CursoService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final RegistroActividadService registroActividadService;

    public CursoServiceImpl(CursoRepository cursoRepository, ProfesorRepository profesorRepository,
                            RegistroActividadService registroActividadService) {
        this.cursoRepository = cursoRepository;
        this.profesorRepository = profesorRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public List<Curso> listarTodos() {
        return cursoRepository.findAllConProfesor();
    }

    @Override
    public List<Curso> listarDestacados() {
        return cursoRepository.findByDestacadoWebTrueAndEliminadoFalse();
    }

    @Override
    public List<Curso> listarPorNivel(Nivel nivel) {
        return cursoRepository.findAllConProfesorByNivel(nivel);
    }

    @Override
    public List<Curso> listarPorNivelYArea(Nivel nivel, String area) {
        return cursoRepository.findByNivelAndArea(nivel, area);
    }

    @Override
    public Page<Curso> buscarPagina(String q, Pageable pageable) {
        return cursoRepository.buscar(q, null, null, pageable);
    }

    @Override
    public Page<Curso> buscarPagina(String q, Nivel nivel, String area, Pageable pageable) {
        return cursoRepository.buscar(q, nivel, area, pageable);
    }

    @Override
    public Map<String, Long> contarPorArea(Nivel nivel) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String area : Areas.paraNivel(nivel)) {
            conteo.put(area, 0L);
        }
        for (Curso curso : cursoRepository.findAllConProfesorByNivel(nivel)) {
            for (String area : curso.getAreas()) {
                if (conteo.containsKey(area)) {
                    conteo.merge(area, 1L, Long::sum);
                }
            }
        }
        return conteo;
    }

    @Override
    public Map<String, List<Curso>> mallaPorNivel(Nivel nivel) {
        Map<String, List<Curso>> malla = new LinkedHashMap<>();
        for (String area : Areas.paraNivel(nivel)) {
            malla.put(area, new ArrayList<>());
        }
        for (Curso curso : cursoRepository.findAllConProfesorByNivel(nivel)) {
            for (String area : curso.getAreas()) {
                if (malla.containsKey(area)) {
                    malla.get(area).add(curso);
                }
            }
        }
        malla.values().removeIf(List::isEmpty);
        return malla;
    }

    @Override
    public Map<Nivel, Long> contarPorNivel() {
        Map<Nivel, Long> conteo = new LinkedHashMap<>();
        for (Nivel nivel : Nivel.values()) {
            conteo.put(nivel, 0L);
        }
        for (Curso curso : listarTodos()) {
            if (curso.getNivel() != null) {
                conteo.merge(curso.getNivel(), 1L, Long::sum);
            }
        }
        return conteo;
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
        boolean esNuevo = (form.getId() == null);
        Curso curso = !esNuevo ? buscarPorId(form.getId()) : new Curso();
        curso.setCodigo(form.getCodigo());
        curso.setNombre(form.getNombre());
        curso.setHoras(form.getHoras());
        curso.setNivel(form.getNivel());
        curso.setDestacadoWeb(form.isDestacadoWeb());

        if (form.getProfesorId() != null) {
            Profesor profesor = profesorRepository.findById(form.getProfesorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado: " + form.getProfesorId()));
            curso.setProfesor(profesor);
        } else {
            curso.setProfesor(null);
        }
        Curso guardado = cursoRepository.save(curso);
        registroActividadService.registrar(esNuevo ? TipoAccion.CREAR : TipoAccion.EDITAR, "Cursos",
                guardado.getId(), (esNuevo ? "Creó el curso " : "Editó el curso ") + guardado.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        cursoRepository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            cursoRepository.save(c);
            registroActividadService.registrar(TipoAccion.ELIMINAR, "Cursos", c.getId(), "Eliminó el curso " + c.getNombre());
        });
    }

    @Override
    @Transactional
    public void establecerCursosDeArea(Nivel nivel, String area, List<Long> cursoIds) {
        Set<Long> idsSeleccionados = new HashSet<>(cursoIds == null ? List.of() : cursoIds);
        int cambios = 0;
        for (Curso curso : cursoRepository.findAllConProfesorByNivel(nivel)) {
            boolean debeEstar = idsSeleccionados.contains(curso.getId());
            boolean estaba = curso.getAreas().contains(area);
            if (debeEstar && !estaba) {
                curso.getAreas().add(area);
                cursoRepository.save(curso);
                cambios++;
            } else if (!debeEstar && estaba) {
                curso.getAreas().remove(area);
                cursoRepository.save(curso);
                cambios++;
            }
        }
        if (cambios > 0) {
            registroActividadService.registrar(TipoAccion.EDITAR, "Áreas", null,
                    "Actualizó los cursos del área " + area + " (" + nivel.getEtiqueta() + ")");
        }
    }

    private CursoForm aForm(Curso c) {
        CursoForm form = new CursoForm();
        form.setId(c.getId());
        form.setCodigo(c.getCodigo());
        form.setNombre(c.getNombre());
        form.setHoras(c.getHoras());
        form.setNivel(c.getNivel());
        form.setProfesorId(c.getProfesor() != null ? c.getProfesor().getId() : null);
        form.setDestacadoWeb(c.isDestacadoWeb());
        return form;
    }
}
