package com.unaj.project.service;

import com.unaj.project.model.RegistroActividad;
import com.unaj.project.model.TipoAccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroActividadService {

    /** Registra una acción a nombre de un usuario explícito (para casos como login/login fallido,
     *  donde todavía no hay Authentication en el SecurityContext). */
    void registrar(String username, TipoAccion accion, String modulo, Long entidadId, String descripcion);

    /** Igual que registrar(username, ...) pero toma el usuario autenticado actual del SecurityContext.
     *  Si no hay nadie autenticado (ej. procesos en segundo plano) no registra nada. */
    void registrar(TipoAccion accion, String modulo, Long entidadId, String descripcion);

    Page<RegistroActividad> buscarPagina(String username, String modulo, TipoAccion accion, String q, Pageable pageable);

    List<RegistroActividad> actividadReciente();

    /** Lista fija de módulos auditados, para poblar el filtro sin depender de qué haya en la BD. */
    List<String> modulos();

    long contarDesde(LocalDateTime desde);
}
