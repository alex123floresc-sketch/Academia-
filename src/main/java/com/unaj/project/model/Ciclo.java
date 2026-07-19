// src/main/java/com/unaj/project/model/Ciclo.java
package com.unaj.project.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ciclos")
public class Ciclo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ejemplo: "Verano 2026", "Anual UNI 2026", "Repaso Intensivo"
    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    // "activo" = ciclo vigente para matricular (NO confundir con borrado lógico)
    @Column(nullable = false)
    private boolean activo;

    // Borrado lógico
    @Column(nullable = false)
    private boolean eliminado = false;

    public Ciclo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
}