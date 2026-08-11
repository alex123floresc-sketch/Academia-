package com.unaj.project.model;

public enum TipoBloque {
    CLASE("Clase"),
    RECESO("Receso");

    private final String etiqueta;

    TipoBloque(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
