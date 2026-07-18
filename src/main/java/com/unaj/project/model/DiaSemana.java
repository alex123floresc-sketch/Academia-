// src/main/java/com/unaj/project/model/DiaSemana.java
package com.unaj.project.model;

public enum DiaSemana {
    LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"),
    JUEVES("Jueves"), VIERNES("Viernes"), SABADO("Sábado");

    private final String etiqueta;
    DiaSemana(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}