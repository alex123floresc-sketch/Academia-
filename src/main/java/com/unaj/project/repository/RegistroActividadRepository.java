package com.unaj.project.repository;

import com.unaj.project.model.RegistroActividad;
import com.unaj.project.model.TipoAccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Long> {

    @Query(value = "SELECT r FROM RegistroActividad r WHERE r.username <> 'desarrollador' AND r.accion <> 'LOGIN' AND " +
            "(:username IS NULL OR :username = '' OR r.username = :username) AND " +
            "(:modulo IS NULL OR :modulo = '' OR r.modulo = :modulo) AND " +
            "(:accion IS NULL OR r.accion = :accion) AND " +
            "(:q IS NULL OR :q = '' OR LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(r.username) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY r.fecha DESC",
            countQuery = "SELECT COUNT(r) FROM RegistroActividad r WHERE r.username <> 'desarrollador' AND r.accion <> 'LOGIN' AND " +
            "(:username IS NULL OR :username = '' OR r.username = :username) AND " +
            "(:modulo IS NULL OR :modulo = '' OR r.modulo = :modulo) AND " +
            "(:accion IS NULL OR r.accion = :accion) AND " +
            "(:q IS NULL OR :q = '' OR LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(r.username) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<RegistroActividad> buscar(@Param("username") String username,
                                   @Param("modulo") String modulo,
                                   @Param("accion") TipoAccion accion,
                                   @Param("q") String q,
                                   Pageable pageable);

    List<RegistroActividad> findTop8ByUsernameNotAndAccionNotOrderByFechaDesc(String username, TipoAccion accion);

    @Query("SELECT COUNT(r) FROM RegistroActividad r WHERE r.username <> 'desarrollador' AND r.accion <> 'LOGIN' AND r.fecha > :desde")
    long countByFechaAfter(@Param("desde") LocalDateTime desde);
}
