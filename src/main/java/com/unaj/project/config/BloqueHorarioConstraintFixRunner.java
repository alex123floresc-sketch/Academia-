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
 * necesita permitir. Primero se consulta information_schema para saber si el índice existe todavía —
 * así el DROP INDEX solo se intenta (y solo aparece en el log) la primera vez que hace falta, en vez
 * de fallar ruidosamente en cada arranque una vez que ya se aplicó la migración.
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
        dropIndiceSiExiste("uk_bloque_ciclo_nivel_turno_hora_area");
        dropIndiceSiExiste("uk_bloque_ciclo_turno_hora_area");
    }

    @SuppressWarnings("unchecked")
    private void dropIndiceSiExiste(String nombreIndice) {
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Number existe = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM information_schema.STATISTICS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bloques_horario' AND INDEX_NAME = :nombre")
                        .setParameter("nombre", nombreIndice)
                        .getSingleResult();
                if (existe.longValue() > 0) {
                    entityManager.createNativeQuery("ALTER TABLE bloques_horario DROP INDEX " + nombreIndice).executeUpdate();
                }
            });
        } catch (Exception e) {
            // No debería pasar ya que se verifica existencia antes, pero si la tabla/BD todavía no
            // existe en este arranque (primera vez, ddl-auto aún no la creó) no es un error real.
        }
    }
}
