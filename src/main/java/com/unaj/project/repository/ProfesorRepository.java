package com.unaj.project.repository;

import com.unaj.project.model.Profesor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    List<Profesor> findByEliminadoFalse();

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmailIgnoreCase(String email);

    // Búsqueda paginada por nombre, apellido, correo o especialidad (q vacío = todos los no eliminados)
    @Query(value = "SELECT p FROM Profesor p WHERE p.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.especialidad) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(p) FROM Profesor p WHERE p.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.especialidad) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Profesor> buscar(@Param("q") String q, Pageable pageable);
}