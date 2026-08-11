package com.unaj.project.model;

public enum TipoAccion {
    CREAR("Creó"),
    EDITAR("Editó"),
    ELIMINAR("Eliminó"),
    LOGIN("Inició sesión"),
    LOGOUT("Cerró sesión"),
    LOGIN_FALLIDO("Intento de inicio de sesión fallido");

    private final String etiqueta;

    TipoAccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
