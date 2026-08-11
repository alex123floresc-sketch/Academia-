package com.unaj.project.dto;

public enum EstadoAsistenciaDia {
    PRESENTE("Asistió"),
    TARDANZA("Tardanza"),
    FALTA("Falta"),
    SIN_CLASE("Sin clase");

    private final String etiqueta;

    EstadoAsistenciaDia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
