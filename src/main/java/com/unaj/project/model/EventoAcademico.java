package com.unaj.project.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "eventos_academicos")
public class EventoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 400)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 60)
    private String tipo;

    @Column(nullable = false)
    private boolean activo = true;

    public EventoAcademico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
