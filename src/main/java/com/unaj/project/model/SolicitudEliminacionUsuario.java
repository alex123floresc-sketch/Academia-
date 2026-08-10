package com.unaj.project.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Eliminar una cuenta con rol ADMIN no es una accion de un solo administrador: requiere que
 * varios administradores esten de acuerdo. Esta solicitud junta las aprobaciones hasta llegar
 * al umbral definido en UsuarioServiceImpl.APROBACIONES_REQUERIDAS, recien ahi se borra la cuenta.
 */
@Entity
@Table(name = "solicitudes_eliminacion_usuario")
public class SolicitudEliminacionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "objetivo_id", nullable = false)
    private Usuario objetivo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitado_por_id", nullable = false)
    private Usuario solicitadoPor;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @ManyToMany
    @JoinTable(
            name = "solicitudes_eliminacion_aprobaciones",
            joinColumns = @JoinColumn(name = "solicitud_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> aprobadoPor = new LinkedHashSet<>();

    public SolicitudEliminacionUsuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getObjetivo() { return objetivo; }
    public void setObjetivo(Usuario objetivo) { this.objetivo = objetivo; }

    public Usuario getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(Usuario solicitadoPor) { this.solicitadoPor = solicitadoPor; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public Set<Usuario> getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Set<Usuario> aprobadoPor) { this.aprobadoPor = aprobadoPor; }
}
