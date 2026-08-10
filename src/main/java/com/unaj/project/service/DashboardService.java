package com.unaj.project.service;

import com.unaj.project.model.Nivel;

import java.util.Map;

public interface DashboardService {
    /** @param nivel nivel para el que se calcula la distribución de alumnos por área/grado. */
    Map<String, Object> resumenInicio(Nivel nivel);
}