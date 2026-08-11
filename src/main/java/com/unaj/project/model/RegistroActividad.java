package com.unaj.project.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro de auditoría inmutable: no referencia a Usuario por FK a propósito, para que el
 * historial sobreviva aunque la cuenta que hizo la acción se borre después.
 */
@Entity
@Table(name = "registros_actividad")
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAccion accion;

    @Column(nullable = false, length = 40)
    private String modulo;

    private Long entidadId;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public TipoAccion getAccion() { return accion; }
    public void setAccion(TipoAccion accion) { this.accion = accion; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
