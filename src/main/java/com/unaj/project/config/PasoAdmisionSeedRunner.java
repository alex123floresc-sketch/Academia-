package com.unaj.project.config;

import com.unaj.project.model.PasoAdmision;
import com.unaj.project.repository.PasoAdmisionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * El "Proceso de admisión" de la página pública pasó de 4 pasos fijos en el HTML a un módulo
 * editable ("pasos_admision"). Este runner siembra esos 4 pasos originales una sola vez, en el
 * primer arranque después de la migración (tabla vacía) — así la sección pública no queda en
 * blanco hasta que un admin la toque. No hay Flyway/Liquibase en este proyecto (ver CLAUDE.md);
 * este es el mecanismo de migración de datos.
 */
@Component
@Order(3)
public class PasoAdmisionSeedRunner implements CommandLineRunner {

    private final PasoAdmisionRepository pasoAdmisionRepository;

    public PasoAdmisionSeedRunner(PasoAdmisionRepository pasoAdmisionRepository) {
        this.pasoAdmisionRepository = pasoAdmisionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (pasoAdmisionRepository.countBy() > 0) {
            return;
        }
        pasoAdmisionRepository.saveAll(List.of(
                paso("Elige tu nivel", "Primaria, Secundaria o Preuniversitario, según la etapa educativa del estudiante.", 1),
                paso("Acércate o contáctanos", "Visita una sede o escríbenos por WhatsApp para resolver tus dudas.", 2),
                paso("Matricúlate", "Completa tu matrícula y elige tus cursos para el ciclo.", 3),
                paso("Empieza tus clases", "Accede a tu horario y comienza a estudiar con nosotros.", 4)
        ));
    }

    private PasoAdmision paso(String titulo, String descripcion, int orden) {
        PasoAdmision p = new PasoAdmision();
        p.setTitulo(titulo);
        p.setDescripcion(descripcion);
        p.setOrden(orden);
        p.setActivo(true);
        return p;
    }
}
