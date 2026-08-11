package com.unaj.project.service;

import com.unaj.project.dto.ConfiguracionForm;
import com.unaj.project.model.Configuracion;

public interface ConfiguracionService {
    Configuracion obtener();
    void actualizar(ConfiguracionForm form);
}
