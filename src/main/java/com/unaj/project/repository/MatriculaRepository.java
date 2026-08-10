package com.unaj.project.repository;

import com.unaj.project.model.Matricula;
import com.unaj.project.model.Nivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

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

    @Query("SELECT m.semestre.nombre, m.turno, COUNT(DISTINCT m.estudiante.id) " +
           "FROM Matricula m WHERE m.estado = 'ACTIVA' AND m.estudiante.nivel = :nivel " +
           "GROUP BY m.semestre.nombre, m.turno " +
           "ORDER BY m.semestre.nombre, m.turno")
    List<Object[]> contarAlumnosPorCicloYTurno(@Param("nivel") Nivel nivel);

    @Query("SELECT DISTINCT m FROM Matricula m " +
           "JOIN FETCH m.semestre " +
           "LEFT JOIN FETCH m.detalles d " +
           "LEFT JOIN FETCH d.curso " +
           "WHERE m.estudiante.id = :estudianteId " +
           "ORDER BY m.fechaMatricula DESC")
    List<Matricula> findByEstudianteIdConDetalle(Long estudianteId);
}
