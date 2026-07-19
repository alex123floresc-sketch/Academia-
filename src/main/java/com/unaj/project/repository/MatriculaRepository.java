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

    // Búsqueda paginada por alumno o ciclo (q vacío = todas)
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

    // Carga completa para mostrar la ficha (evita LazyInitializationException)
    @Query("SELECT DISTINCT m FROM Matricula m " +
            "JOIN FETCH m.estudiante " +
            "JOIN FETCH m.semestre " +
            "LEFT JOIN FETCH m.detalles d " +
            "LEFT JOIN FETCH d.curso c " +
            "LEFT JOIN FETCH c.profesor " +      // antes: c.docente
            "WHERE m.id = :id")
    Optional<Matricula> findByIdConDetalle(Long id);
    // Para el listado general, con estudiante, semestre, detalles y curso (para totalCreditos)
    @Query("SELECT DISTINCT m FROM Matricula m " +
           "JOIN FETCH m.estudiante " +
           "JOIN FETCH m.semestre " +
           "LEFT JOIN FETCH m.detalles d " +
           "LEFT JOIN FETCH d.curso")
    List<Matricula> findAllConEstudianteYSemestre();

    // Reporte: cantidad de alumnos matriculados (activos) por ciclo y turno
    @Query("SELECT m.semestre.nombre, m.turno, COUNT(DISTINCT m.estudiante.id) " +
           "FROM Matricula m WHERE m.estado = 'ACTIVA' " +
           "GROUP BY m.semestre.nombre, m.turno " +
           "ORDER BY m.semestre.nombre, m.turno")
    List<Object[]> contarAlumnosPorCicloYTurno();

    // Expediente del alumno: todas sus matrículas con ciclo, detalles y curso (evita LazyInitializationException)
    @Query("SELECT DISTINCT m FROM Matricula m " +
           "JOIN FETCH m.semestre " +
           "LEFT JOIN FETCH m.detalles d " +
           "LEFT JOIN FETCH d.curso " +
           "WHERE m.estudiante.id = :estudianteId " +
           "ORDER BY m.fechaMatricula DESC")
    List<Matricula> findByEstudianteIdConDetalle(Long estudianteId);
}
