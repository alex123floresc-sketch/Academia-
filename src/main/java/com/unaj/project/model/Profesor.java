package com.unaj.project.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "profesores")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String especialidad;

    @Column(precision = 8, scale = 2)
    private BigDecimal tarifaHora;

    @Column(nullable = false)
    private boolean eliminado = false;

    @Lob
    @Column(name = "foto", columnDefinition = "LONGBLOB")
    private byte[] foto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    @ElementCollection
    @CollectionTable(name = "profesor_niveles", joinColumns = @JoinColumn(name = "profesor_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private Set<Nivel> niveles = new LinkedHashSet<>();

    @Column(name = "destacado_web", nullable = false)
    private boolean destacadoWeb = false;

    public Profesor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public BigDecimal getTarifaHora() { return tarifaHora; }
    public void setTarifaHora(BigDecimal tarifaHora) { this.tarifaHora = tarifaHora; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }

    public String getFotoContentType() { return fotoContentType; }
    public void setFotoContentType(String fotoContentType) { this.fotoContentType = fotoContentType; }

    public boolean isFotoPresente() { return foto != null && foto.length > 0; }

    public Set<Nivel> getNiveles() { return niveles; }
    public void setNiveles(Set<Nivel> niveles) { this.niveles = niveles; }

    public boolean isDestacadoWeb() { return destacadoWeb; }
    public void setDestacadoWeb(boolean destacadoWeb) { this.destacadoWeb = destacadoWeb; }

    public String getNombreCompleto() { return nombre + " " + apellido; }
}