package com.unaj.project.service;

import java.util.Map;

public interface DashboardService {
    /** Arma todos los datos del panel de inicio en un mapa listo para la vista. */
    Map<String, Object> resumenInicio();
}