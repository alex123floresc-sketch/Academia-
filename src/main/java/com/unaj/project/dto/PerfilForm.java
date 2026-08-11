package com.unaj.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class PerfilForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    private String nombre;

    private MultipartFile foto;

    private boolean quitarFoto;

    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String email;

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{6,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    @Size(max = 80, message = "Máximo 80 caracteres")
    private String cargo;

    private MultipartFile firma;

    private boolean quitarFirma;

    private String passwordActual;

    private String passwordNueva;

    private String passwordNuevaConfirmar;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public boolean isQuitarFoto() { return quitarFoto; }
    public void setQuitarFoto(boolean quitarFoto) { this.quitarFoto = quitarFoto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public MultipartFile getFirma() { return firma; }
    public void setFirma(MultipartFile firma) { this.firma = firma; }

    public boolean isQuitarFirma() { return quitarFirma; }
    public void setQuitarFirma(boolean quitarFirma) { this.quitarFirma = quitarFirma; }

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }

    public String getPasswordNuevaConfirmar() { return passwordNuevaConfirmar; }
    public void setPasswordNuevaConfirmar(String passwordNuevaConfirmar) { this.passwordNuevaConfirmar = passwordNuevaConfirmar; }
}
