package com.unaj.project.dto;

import com.unaj.project.model.Nivel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SolicitudInformacionForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+()\\-\\s]{6,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String correo;

    private Nivel nivelInteres;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String mensaje;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Nivel getNivelInteres() { return nivelInteres; }
    public void setNivelInteres(Nivel nivelInteres) { this.nivelInteres = nivelInteres; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
