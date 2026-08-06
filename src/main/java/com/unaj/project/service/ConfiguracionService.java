package com.unaj.project.service;

import com.unaj.project.model.Configuracion;

import java.math.BigDecimal;

public interface ConfiguracionService {
    Configuracion obtener();
    void actualizar(BigDecimal montoMatricula, BigDecimal montoPension);
}
