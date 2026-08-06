package com.unaj.project.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    private Long id = 1L;

    @Column(name = "monto_matricula", nullable = false)
    private BigDecimal montoMatricula;

    @Column(name = "monto_pension", nullable = false)
    private BigDecimal montoPension;

    public Configuracion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontoMatricula() { return montoMatricula; }
    public void setMontoMatricula(BigDecimal montoMatricula) { this.montoMatricula = montoMatricula; }

    public BigDecimal getMontoPension() { return montoPension; }
    public void setMontoPension(BigDecimal montoPension) { this.montoPension = montoPension; }
}
