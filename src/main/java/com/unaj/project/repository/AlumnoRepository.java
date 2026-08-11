package com.unaj.project.repository;

import com.unaj.project.model.Alumno;
import com.unaj.project.model.Nivel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByEliminadoFalse();

    @Modifying
    @Query("UPDATE Alumno a SET a.nivel = :nivel WHERE a.nivel IS NULL")
    int backfillNivel(@Param("nivel") Nivel nivel);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByDni(String dni);

    Optional<Alumno> findByDni(String dni);

    /** salonId: null = sin filtrar por salón, -1 = solo alumnos sin salón asignado, cualquier otro id = ese salón. */
    @Query(value = "SELECT a FROM Alumno a WHERE a.eliminado = false " +
            "AND (:nivel IS NULL OR a.nivel = :nivel) " +
            "AND (:area IS NULL OR :area = '' OR LOWER(a.area) = LOWER(:area)) " +
            "AND (:salonId IS NULL OR (:salonId = -1 AND a.salon IS NULL) OR a.salon.id = :salonId) " +
            "AND (:q IS NULL OR :q = '' " +
            "OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(a) FROM Alumno a WHERE a.eliminado = false " +
            "AND (:nivel IS NULL OR a.nivel = :nivel) " +
            "AND (:area IS NULL OR :area = '' OR LOWER(a.area) = LOWER(:area)) " +
            "AND (:salonId IS NULL OR (:salonId = -1 AND a.salon IS NULL) OR a.salon.id = :salonId) " +
            "AND (:q IS NULL OR :q = '' " +
            "OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Alumno> buscar(@Param("q") String q, @Param("nivel") Nivel nivel, @Param("area") String area,
                        @Param("salonId") Long salonId, Pageable pageable);

    long countBySalonIdAndEliminadoFalse(Long salonId);
}
