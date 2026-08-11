package com.unaj.project.dto;

import com.unaj.project.model.Nivel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProfesorForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    private String especialidad;

    @DecimalMin(value = "0.0", message = "La tarifa no puede ser negativa")
    private BigDecimal tarifaHora;

    private Set<Nivel> niveles = new LinkedHashSet<>();

    private MultipartFile foto;

    private boolean destacadoWeb;

    public ProfesorForm() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public BigDecimal getTarifaHora() { return tarifaHora; }
    public void setTarifaHora(BigDecimal tarifaHora) { this.tarifaHora = tarifaHora; }

    public Set<Nivel> getNiveles() { return niveles; }
    public void setNiveles(Set<Nivel> niveles) { this.niveles = niveles; }

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public boolean isDestacadoWeb() { return destacadoWeb; }
    public void setDestacadoWeb(boolean destacadoWeb) { this.destacadoWeb = destacadoWeb; }
}