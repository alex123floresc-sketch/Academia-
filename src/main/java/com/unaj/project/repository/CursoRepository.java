package com.unaj.project.repository;

import com.unaj.project.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Curso findByCodigo(String codigo);

    // Trae el profesor junto con el curso en una sola consulta (evita LazyInitializationException)
    // Solo cursos no eliminados
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.eliminado = false")
    List<Curso> findAllConProfesor();

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id = :id")
    Optional<Curso> findByIdConProfesor(Long id);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id IN :ids")
    List<Curso> findAllByIdConProfesor(List<Long> ids);
}