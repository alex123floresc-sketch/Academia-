package com.unaj.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class SedeForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String direccion;

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{6,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String horario;

    private boolean activa = true;

    private MultipartFile foto;

    private boolean quitarFoto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public boolean isQuitarFoto() { return quitarFoto; }
    public void setQuitarFoto(boolean quitarFoto) { this.quitarFoto = quitarFoto; }
}
