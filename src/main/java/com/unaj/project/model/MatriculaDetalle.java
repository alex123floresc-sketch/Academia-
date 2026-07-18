package com.unaj.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "matricula_detalles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"matricula_id", "curso_id"})
})
public class MatriculaDetalle {
it
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    public MatriculaDetalle() {}

    public MatriculaDetalle(Curso curso) {
        this.curso = curso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Matricula getMatricula() { return matricula; }
    public void setMatricula(Matricula matricula) { this.matricula = matricula; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}
