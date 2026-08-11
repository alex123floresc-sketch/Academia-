package com.unaj.project.service.impl;

import com.unaj.project.model.RegistroActividad;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.RegistroActividadRepository;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistroActividadServiceImpl implements RegistroActividadService {

    private static final String USERNAME_DESARROLLADOR = "desarrollador";

    private static final List<String> MODULOS = List.of(
            "Alumnos", "Cursos", "Profesores", "Ciclos", "Matrículas", "Pagos",
            "Horarios", "Áreas", "Configuración", "Horas docentes", "Usuarios", "Sesión", "Sedes",
            "Preguntas frecuentes", "Testimonios", "Resultados de ingreso", "Galería de fotos",
            "Calendario académico", "Solicitudes de información", "Proceso de admisión");

    private final RegistroActividadRepository repository;

    public RegistroActividadServiceImpl(RegistroActividadRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void registrar(String username, TipoAccion accion, String modulo, Long entidadId, String descripcion) {
        if (username == null || username.isBlank()) {
            return;
        }
        RegistroActividad registro = new RegistroActividad();
        registro.setUsername(username);
        registro.setAccion(accion);
        registro.setModulo(modulo);
        registro.setEntidadId(entidadId);
        registro.setDescripcion(descripcion);
        registro.setFecha(LocalDateTime.now());
        repository.save(registro);
    }

    @Override
    public void registrar(TipoAccion accion, String modulo, Long entidadId, String descripcion) {
        registrar(usuarioActual(), accion, modulo, entidadId, descripcion);
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        return auth.getName();
    }

    @Override
    public Page<RegistroActividad> buscarPagina(String username, String modulo, TipoAccion accion, String q, Pageable pageable) {
        return repository.buscar(username, modulo, accion, q, pageable);
    }

    @Override
    public List<RegistroActividad> actividadReciente() {
        return repository.findTop8ByUsernameNotOrderByFechaDesc(USERNAME_DESARROLLADOR);
    }

    @Override
    public List<String> modulos() {
        return MODULOS;
    }

    @Override
    public long contarDesde(LocalDateTime desde) {
        return repository.countByFechaAfter(desde);
    }
}
