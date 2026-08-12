package com.unaj.project.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * "bloques_horario" pasó de una restricción única (ciclo, nivel, turno, hora_inicio, area) a una que
 * también incluye salon_id, para permitir que dos salones de Preuniversitario tengan cada uno su
 * propio bloque en el mismo ciclo+turno+hora+área. ddl-auto=update agrega la restricción nueva pero
 * NO borra la vieja (mismo patrón ya documentado en NivelBackfillRunner para bloques_horario), así
 * que sin este runner la restricción vieja seguiría bloqueando exactamente el caso que esta funcionalidad
 * necesita permitir. DROP INDEX falla si el índice no existe (por ejemplo, la primera vez que este
 * runner corre exitosamente, o en una base de datos nueva que nunca tuvo la restricción vieja) — cada
 * intento corre en su propia transacción (TransactionTemplate, no el EntityManager compartido
 * directamente, porque Spring no permite manejar transacciones a mano sobre un EntityManager
 * inyectado) para que ese fallo esperado no arrastre ni bloquee al otro intento.
 * No hay Flyway/Liquibase en este proyecto (ver CLAUDE.md); este es el mecanismo de migración de datos.
 */
@Component
@Order(4)
public class BloqueHorarioConstraintFixRunner implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    public BloqueHorarioConstraintFixRunner(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void run(String... args) {
        intentarDropIndex("uk_bloque_ciclo_nivel_turno_hora_area");
        intentarDropIndex("uk_bloque_ciclo_turno_hora_area");
    }

    private void intentarDropIndex(String nombreIndice) {
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    entityManager.createNativeQuery("ALTER TABLE bloques_horario DROP INDEX " + nombreIndice).executeUpdate());
        } catch (Exception e) {
            // El índice ya no existe (ya se corrió antes, o nunca existió en esta base de datos) — esperado.
        }
    }
}
