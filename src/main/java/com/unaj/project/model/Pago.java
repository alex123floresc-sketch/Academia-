// src/main/java/com/unaj/project/model/Pago.java
package com.unaj.project.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    // "Matrícula", "Pensión febrero", etc.
    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    // Suma de los abonos recibidos hasta ahora. Nullable (columna agregada sobre una tabla
    // ya poblada); se trata como CERO cuando es null.
    @Column(precision = 8, scale = 2)
    private BigDecimal montoPagado;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    // null mientras no se pague
    @Column
    private LocalDateTime fechaPago;

    // PENDIENTE, PARCIAL, PAGADO, VENCIDO
    @Column(nullable = false)
    private String estado;

    // EFECTIVO, YAPE, TRANSFERENCIA
    @Column
    private String metodo;

    public Pago() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Matricula getMatricula() { return matricula; }
    public void setMatricula(Matricula matricula) { this.matricula = matricula; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public BigDecimal getMontoPagado() { return montoPagado != null ? montoPagado : BigDecimal.ZERO; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }

    public BigDecimal getSaldo() { return monto.subtract(getMontoPagado()); }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}