package com.unaj.project.config;

import com.unaj.project.model.Nivel;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.BloqueHorarioRepository;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.ProfesorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Todo lo que existía antes de introducir el concepto de Nivel (Primaria/Secundaria/Preuniversitario)
 * pertenece al negocio de Preuniversitario (Ingenierías/Biomédicas/Sociales). Este runner asigna
 * ese nivel por defecto a cualquier registro que todavía no tenga uno, en cada arranque — es
 * idempotente (WHERE nivel IS NULL / sin niveles), así que no repite trabajo una vez migrados.
 * No hay Flyway/Liquibase en este proyecto (ver CLAUDE.md); este es el mecanismo de migración de datos.
 */
@Component
@Order(1)
public class NivelBackfillRunner implements CommandLineRunner {

    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final ProfesorRepository profesorRepository;

    public NivelBackfillRunner(AlumnoRepository alumnoRepository,
                               CursoRepository cursoRepository,
                               BloqueHorarioRepository bloqueHorarioRepository,
                               ProfesorRepository profesorRepository) {
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.bloqueHorarioRepository = bloqueHorarioRepository;
        this.profesorRepository = profesorRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        alumnoRepository.backfillNivel(Nivel.PREUNIVERSITARIO);
        cursoRepository.backfillNivel(Nivel.PREUNIVERSITARIO);
        bloqueHorarioRepository.backfillNivel(Nivel.PREUNIVERSITARIO);
        profesorRepository.backfillNivelesVacios();
    }
}
