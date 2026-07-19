// src/main/java/com/unaj/project/model/Matricula.java
package com.unaj.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo semestre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)         // nullable para no romper filas existentes
    private Turno turno;

    @Column(nullable = false)
    private LocalDateTime fechaMatricula;

    // ACTIVA, ANULADA, RESERVADA
    @Column(nullable = false)
    private String estado;

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MatriculaDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pago> pagos = new ArrayList<>();

    public Matricula() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumno getEstudiante() { return estudiante; }
    public void setEstudiante(Alumno estudiante) { this.estudiante = estudiante; }

    public Ciclo getSemestre() { return semestre; }
    public void setSemestre(Ciclo semestre) { this.semestre = semestre; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public LocalDateTime getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(LocalDateTime fechaMatricula) { this.fechaMatricula = fechaMatricula; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<MatriculaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<MatriculaDetalle> detalles) { this.detalles = detalles; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }

    public void addDetalle(MatriculaDetalle detalle) {
        detalles.add(detalle);
        detalle.setMatricula(this);
    }

    public void addPago(Pago pago) {
        pagos.add(pago);
        pago.setMatricula(this);
    }

    public int getTotalHoras() {
        return detalles.stream()
                .mapToInt(d -> d.getCurso().getHoras())
                .sum();
    }
}