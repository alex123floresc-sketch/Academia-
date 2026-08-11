package com.unaj.project.config;

import com.unaj.project.repository.ConfiguracionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las columnas "mostrar_proceso_admision"/"mostrar_faq"/"mostrar_testimonios"/"mostrar_logros"/
 * "mostrar_galeria"/"mostrar_calendario"/"mostrar_formulario_contacto" se agregaron a una fila de
 * "configuracion" que ya podía existir de antes; ddl-auto=update las crea en false (no aplica el
 * valor por defecto de Java a filas existentes), así que sin este backfill las secciones nuevas de
 * la página pública quedarían apagadas silenciosamente. Solo enciende las siete si TODAS están en
 * false a la vez (ninguna fila real tendría motivo para desactivarlas todas simultáneamente), así
 * que es seguro re-ejecutarlo en cada arranque sin pisar una decisión explícita del admin.
 * No hay Flyway/Liquibase en este proyecto (ver CLAUDE.md); este es el mecanismo de migración de datos.
 */
@Component
@Order(2)
public class ConfiguracionBackfillRunner implements CommandLineRunner {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionBackfillRunner(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        configuracionRepository.backfillMostrarPaginaWebSiTodoApagado();
    }
}
