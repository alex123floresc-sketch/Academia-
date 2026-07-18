package com.unaj.project.repository;

import com.unaj.project.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByEliminadoFalse();

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmailIgnoreCase(String email);

    // Búsqueda paginada por nombre, apellido o correo (q vacío = todos los no eliminados)
    @Query(value = "SELECT a FROM Alumno a WHERE a.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(a) FROM Alumno a WHERE a.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Alumno> buscar(@Param("q") String q, Pageable pageable);
}