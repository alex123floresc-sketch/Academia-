package com.unaj.project.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bloque_id", "dia_semana", "curso_id"})
})
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloque_id", nullable = false)
    private BloqueHorario bloque;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DiaSemana diaSemana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column
    private String aula;

    public Horario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BloqueHorario getBloque() { return bloque; }
    public void setBloque(BloqueHorario bloque) { this.bloque = bloque; }

    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public String getAula() { return aula; }
    public void setAula(String aula) { this.aula = aula; }

    public Turno getTurno() { return bloque != null ? bloque.getTurno() : null; }
    public LocalTime getHoraInicio() { return bloque != null ? bloque.getHoraInicio() : null; }
    public LocalTime getHoraFin() { return bloque != null ? bloque.getHoraFin() : null; }
    public Ciclo getCiclo() { return bloque != null ? bloque.getCiclo() : null; }
    public String getArea() { return bloque != null ? bloque.getArea() : null; }
    public Nivel getNivel() { return bloque != null ? bloque.getNivel() : null; }
}
