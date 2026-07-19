package com.unaj.project.repository;

import com.unaj.project.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Curso findByCodigo(String codigo);

    // Búsqueda paginada por nombre, código o profesor (q vacío = todos los no eliminados)
    @Query(value = "SELECT c FROM Curso c LEFT JOIN c.profesor p WHERE c.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(c) FROM Curso c LEFT JOIN c.profesor p WHERE c.eliminado = false AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Curso> buscar(@Param("q") String q, Pageable pageable);

    // Trae el profesor junto con el curso en una sola consulta (evita LazyInitializationException)
    // Solo cursos no eliminados
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.eliminado = false")
    List<Curso> findAllConProfesor();

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id = :id")
    Optional<Curso> findByIdConProfesor(Long id);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id IN :ids")
    List<Curso> findAllByIdConProfesor(List<Long> ids);
}