package com.unaj.project.service;

import com.unaj.project.dto.FotoGaleriaForm;
import com.unaj.project.model.FotoGaleria;

import java.util.List;

public interface FotoGaleriaService {

    List<FotoGaleria> listarTodas();

    List<FotoGaleria> listarActivas();

    FotoGaleria buscarPorId(Long id);

    FotoGaleriaForm buscarFormPorId(Long id);

    void guardar(FotoGaleriaForm form);

    void eliminar(Long id);
}
