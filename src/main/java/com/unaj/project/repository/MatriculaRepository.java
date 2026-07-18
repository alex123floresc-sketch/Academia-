package com.unaj.project.repository;

import com.unaj.project.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

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
}
