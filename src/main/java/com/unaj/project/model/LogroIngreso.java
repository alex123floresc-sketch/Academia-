package com.unaj.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "logros_ingreso")
public class LogroIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombreAlumno;

    @Column(nullable = false, length = 120)
    private String universidad;

    @Column(length = 120)
    private String carrera;

    @Column(name = "anio_ingreso")
    private Integer anioIngreso;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false)
    private boolean activo = true;

    @Lob
    @Column(name = "foto", columnDefinition = "LONGBLOB")
    private byte[] foto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    public LogroIngreso() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getUniversidad() { return universidad; }
    public void setUniversidad(String universidad) { this.universidad = universidad; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public Integer getAnioIngreso() { return anioIngreso; }
    public void setAnioIngreso(Integer anioIngreso) { this.anioIngreso = anioIngreso; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }

    public String getFotoContentType() { return fotoContentType; }
    public void setFotoContentType(String fotoContentType) { this.fotoContentType = fotoContentType; }

    public boolean isFotoPresente() { return foto != null && foto.length > 0; }
}
