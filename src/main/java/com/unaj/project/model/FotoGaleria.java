package com.unaj.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fotos_galeria")
public class FotoGaleria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String descripcion;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false)
    private boolean activa = true;

    @Lob
    @Column(name = "imagen", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imagen;

    @Column(name = "imagen_content_type")
    private String imagenContentType;

    public FotoGaleria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    public String getImagenContentType() { return imagenContentType; }
    public void setImagenContentType(String imagenContentType) { this.imagenContentType = imagenContentType; }

    public boolean isImagenPresente() { return imagen != null && imagen.length > 0; }
}
