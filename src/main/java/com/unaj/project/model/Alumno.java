package com.unaj.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true)
    private String email;

    @Column
    private String celular;

    @Column(length = 8)
    private String dni;

    @Column(name = "nombre_padre")
    private String nombrePadre;

    @Column(name = "telefono_padre")
    private String telefonoPadre;

    @Column(name = "area")
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private Nivel nivel;

    @Lob
    @Column(name = "foto", columnDefinition = "LONGBLOB")
    private byte[] foto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    @Column(nullable = false)
    private boolean eliminado = false;

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombrePadre() { return nombrePadre; }
    public void setNombrePadre(String nombrePadre) { this.nombrePadre = nombrePadre; }

    public String getTelefonoPadre() { return telefonoPadre; }
    public void setTelefonoPadre(String telefonoPadre) { this.telefonoPadre = telefonoPadre; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }

    public String getFotoContentType() { return fotoContentType; }
    public void setFotoContentType(String fotoContentType) { this.fotoContentType = fotoContentType; }

    public boolean isFotoPresente() { return foto != null && foto.length > 0; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public String getNombreCompleto() { return nombre + " " + apellido; }
}