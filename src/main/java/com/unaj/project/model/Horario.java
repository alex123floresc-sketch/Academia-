package com.unaj.project.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jornada_id")
    private Jornada jornada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column
    private String aula;

    public Horario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Jornada getJornada() { return jornada; }
    public void setJornada(Jornada jornada) { this.jornada = jornada; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public String getAula() { return aula; }
    public void setAula(String aula) { this.aula = aula; }

    public DiaSemana getDiaSemana() { return jornada != null ? jornada.getDiaSemana() : null; }
    public Turno getTurno() { return jornada != null ? jornada.getTurno() : null; }
    public LocalTime getHoraInicio() { return jornada != null ? jornada.getHoraInicio() : null; }
    public LocalTime getHoraFin() { return jornada != null ? jornada.getHoraFin() : null; }
    public Ciclo getCiclo() { return jornada != null ? jornada.getCiclo() : null; }
}
