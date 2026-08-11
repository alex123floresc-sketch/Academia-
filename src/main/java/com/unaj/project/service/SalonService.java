package com.unaj.project.service;

import com.unaj.project.dto.SalonForm;
import com.unaj.project.model.Salon;

import java.util.List;

public interface SalonService {

    List<Salon> listarTodos();

    List<Salon> listarActivos();

    Salon buscarPorId(Long id);

    SalonForm buscarFormPorId(Long id);

    void guardar(SalonForm form);

    void eliminar(Long id);
}
