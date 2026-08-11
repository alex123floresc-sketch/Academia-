package com.unaj.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class TestimonioForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;

    @Size(max = 80, message = "Máximo 80 caracteres")
    private String rol;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 800, message = "Máximo 800 caracteres")
    private String comentario;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden = 0;

    private boolean activo = true;

    private MultipartFile foto;

    private boolean quitarFoto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public boolean isQuitarFoto() { return quitarFoto; }
    public void setQuitarFoto(boolean quitarFoto) { this.quitarFoto = quitarFoto; }
}
