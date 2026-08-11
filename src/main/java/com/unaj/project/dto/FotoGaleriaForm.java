package com.unaj.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class FotoGaleriaForm {

    private Long id;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String descripcion;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden = 0;

    private boolean activa = true;

    private MultipartFile imagen;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public MultipartFile getImagen() { return imagen; }
    public void setImagen(MultipartFile imagen) { this.imagen = imagen; }
}
