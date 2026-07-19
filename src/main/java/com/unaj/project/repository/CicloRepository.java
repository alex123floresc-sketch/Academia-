package com.unaj.project.repository;

import com.unaj.project.model.Ciclo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CicloRepository extends JpaRepository<Ciclo, Long> {

    // Solo el ciclo vigente (activo) que no esté eliminado
    Ciclo findFirstByActivoTrueAndEliminadoFalse();

    List<Ciclo> findByEliminadoFalse();

    // Búsqueda paginada por nombre (q vacío = todos los no eliminados)
    @Query(value = "SELECT c FROM Ciclo c WHERE c.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(c) FROM Ciclo c WHERE c.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Ciclo> buscar(@Param("q") String q, Pageable pageable);
}