package com.unaj.project.repository;

import com.unaj.project.model.Matricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    @Query(value = "SELECT m FROM Matricula m JOIN FETCH m.estudiante e JOIN FETCH m.semestre s " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(m) FROM Matricula m JOIN m.estudiante e JOIN m.semestre s " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Matricula> buscar(@Param("q") String q, Pageable pageable);

    List<Matricula> findByEstudianteId(Long estudianteId);

    Optional<Matricula> findByEstudianteIdAndSemestreId(Long estudianteId, Long semestreId);

    List<Matricula> findBySemestreId(Long semestreId);

    @Query("SELECT DISTINCT m FROM Matricula m " +
            "JOIN FETCH m.estudiante " +
            "JOIN FETCH m.semestre " +
            "LEFT JOIN FETCH m.detalles d " +
            "LEFT JOIN FETCH d.curso c " +
            "LEFT JOIN FETCH c.profesor " +
            "WHERE m.id = :id")
    Optional<Matricula> findByIdConDetalle(Long id);
    @Query("SELECT DISTINCT m FROM Matricula m " +
           "JOIN FETCH m.estudiante " +
           "JOIN FETCH m.semestre " +
           "LEFT JOIN FETCH m.detalles d " +
           "LEFT JOIN FETCH d.curso")
    List<Matricula> findAllConEstudianteYSemestre();

    @Query("SELECT m.semestre.nombre, m.turno, COUNT(DISTINCT m.estudiante.id) " +
           "FROM Matricula m WHERE m.estado = 'ACTIVA' " +
           "GROUP BY m.semestre.nombre, m.turno " +
           "ORDER BY m.semestre.nombre, m.turno")
    List<Object[]> contarAlumnosPorCicloYTurno();

    @Query("SELECT DISTINCT m FROM Matricula m " +
           "JOIN FETCH m.semestre " +
           "LEFT JOIN FETCH m.detalles d " +
           "LEFT JOIN FETCH d.curso " +
           "WHERE m.estudiante.id = :estudianteId " +
           "ORDER BY m.fechaMatricula DESC")
    List<Matricula> findByEstudianteIdConDetalle(Long estudianteId);
}
