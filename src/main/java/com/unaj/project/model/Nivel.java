package com.unaj.project.model;

public enum Nivel {
    PRIMARIA("Primaria"),
    SECUNDARIA("Secundaria"),
    PREUNIVERSITARIO("Preuniversitario");

    private final String etiqueta;

    Nivel(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
