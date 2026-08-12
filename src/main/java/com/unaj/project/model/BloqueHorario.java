package com.unaj.project.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "bloques_horario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bloque_ciclo_nivel_turno_hora_area_salon",
                columnNames = {"ciclo_id", "nivel", "turno", "hora_inicio", "area", "salon_id"})
})
public class BloqueHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo ciclo;

    /** Solo se usa en Preuniversitario cuando hay más de un salón dictando la misma área en paralelo;
     * cada salón tiene así su propio horario, totalmente independiente de los demás. Null en el resto de casos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Turno turno;

    @Column(nullable = false)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private Nivel nivel;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBloque tipo;

    public BloqueHorario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ciclo getCiclo() { return ciclo; }
    public void setCiclo(Ciclo ciclo) { this.ciclo = ciclo; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public TipoBloque getTipo() { return tipo; }
    public void setTipo(TipoBloque tipo) { this.tipo = tipo; }

    public boolean isReceso() { return tipo == TipoBloque.RECESO; }
}
