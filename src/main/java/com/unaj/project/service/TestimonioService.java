package com.unaj.project.service;

import com.unaj.project.dto.TestimonioForm;
import com.unaj.project.model.Testimonio;

import java.util.List;

public interface TestimonioService {

    List<Testimonio> listarTodos();

    List<Testimonio> listarActivos();

    Testimonio buscarPorId(Long id);

    TestimonioForm buscarFormPorId(Long id);

    void guardar(TestimonioForm form);

    void eliminar(Long id);
}
