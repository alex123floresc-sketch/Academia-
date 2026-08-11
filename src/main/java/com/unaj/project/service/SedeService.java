package com.unaj.project.service;

import com.unaj.project.dto.SedeForm;
import com.unaj.project.model.Sede;

import java.util.List;

public interface SedeService {

    List<Sede> listarTodas();

    List<Sede> listarActivas();

    Sede buscarPorId(Long id);

    SedeForm buscarFormPorId(Long id);

    void guardar(SedeForm form);

    void eliminar(Long id);
}
