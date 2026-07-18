// src/main/java/com/unaj/project/model/Turno.java
package com.unaj.project.model;

public enum Turno {
    MANANA("Mañana"),
    TARDE("Tarde"),
    NOCHE("Noche");

    private final String etiqueta;

    Turno(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}