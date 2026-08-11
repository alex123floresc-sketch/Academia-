package com.unaj.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PreguntaFrecuenteForm {

    private Long id;

    @NotBlank(message = "La pregunta es obligatoria")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String pregunta;

    @NotBlank(message = "La respuesta es obligatoria")
    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String respuesta;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden = 0;

    private boolean activa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
