// src/main/java/com/unaj/project/model/Alumno.java
package com.unaj.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")   // se conserva el nombre físico de la tabla
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String celular;

    // Área a la que postula (Ingenierías, Biomédicas, Sociales).
    // Reutiliza la columna "carrera" para no perder datos existentes.
    @Column(name = "carrera")
    private String area;

    // Borrado lógico: true = registro "eliminado" (oculto de los listados)
    @Column(nullable = false)
    private boolean eliminado = false;

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public String getNombreCompleto() { return nombre + " " + apellido; }
}