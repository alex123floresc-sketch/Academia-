package com.unaj.project.service;

import com.unaj.project.dto.EventoAcademicoForm;
import com.unaj.project.model.EventoAcademico;

import java.util.List;

public interface EventoAcademicoService {

    List<EventoAcademico> listarTodos();

    /** Próximos eventos activos, desde hoy en adelante, ordenados por fecha. */
    List<EventoAcademico> listarProximos();

    EventoAcademico buscarPorId(Long id);

    EventoAcademicoForm buscarFormPorId(Long id);

    void guardar(EventoAcademicoForm form);

    void eliminar(Long id);
}
