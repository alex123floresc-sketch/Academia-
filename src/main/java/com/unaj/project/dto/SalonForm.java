package com.unaj.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SalonForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 40, message = "Máximo 40 caracteres")
    private String nombre;

    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
