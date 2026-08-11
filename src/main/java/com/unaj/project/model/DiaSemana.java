package com.unaj.project.model;

import java.time.DayOfWeek;

public enum DiaSemana {
    LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"),
    JUEVES("Jueves"), VIERNES("Viernes"), SABADO("Sábado");

    private final String etiqueta;
    DiaSemana(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }

    public static DiaSemana desde(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> LUNES;
            case TUESDAY -> MARTES;
            case WEDNESDAY -> MIERCOLES;
            case THURSDAY -> JUEVES;
            case FRIDAY -> VIERNES;
            case SATURDAY -> SABADO;
            case SUNDAY -> null;
        };
    }
}