package com.unaj.project.service;

import com.unaj.project.dto.LogroIngresoForm;
import com.unaj.project.model.LogroIngreso;

import java.util.List;

public interface LogroIngresoService {

    List<LogroIngreso> listarTodos();

    List<LogroIngreso> listarActivos();

    LogroIngreso buscarPorId(Long id);

    LogroIngresoForm buscarFormPorId(Long id);

    void guardar(LogroIngresoForm form);

    void eliminar(Long id);
}
