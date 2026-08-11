package com.unaj.project.model;

import jakarta.persistence.*;

/** Salón físico de Preuniversitario, usado para agrupar alumnos de ese nivel para mejor manejo de datos. */
@Entity
@Table(name = "salones")
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Column
    private Integer capacidad;

    @Column(nullable = false)
    private boolean activo = true;

    public Salon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
