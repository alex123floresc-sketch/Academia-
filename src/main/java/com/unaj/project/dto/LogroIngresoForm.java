package com.unaj.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class LogroIngresoForm {

    private Long id;

    @NotBlank(message = "El nombre del alumno es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombreAlumno;

    @NotBlank(message = "La universidad es obligatoria")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String universidad;

    @Size(max = 120, message = "Máximo 120 caracteres")
    private String carrera;

    private Integer anioIngreso;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden = 0;

    private boolean activo = true;

    private MultipartFile foto;

    private boolean quitarFoto;

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

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public boolean isQuitarFoto() { return quitarFoto; }
    public void setQuitarFoto(boolean quitarFoto) { this.quitarFoto = quitarFoto; }
}
