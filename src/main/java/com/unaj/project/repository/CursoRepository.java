package com.unaj.project.repository;

import com.unaj.project.model.Curso;
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
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Curso findByCodigo(String codigo);

    @Modifying
    @Query("UPDATE Curso c SET c.nivel = :nivel WHERE c.nivel IS NULL")
    int backfillNivel(@Param("nivel") Nivel nivel);

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

    @Query(value = "SELECT DISTINCT c FROM Curso c LEFT JOIN c.profesor p LEFT JOIN c.areas ar " +
            "WHERE c.eliminado = false " +
            "AND (:nivel IS NULL OR c.nivel = :nivel) " +
            "AND (:area IS NULL OR :area = '' OR ar = :area) " +
            "AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c) FROM Curso c LEFT JOIN c.profesor p LEFT JOIN c.areas ar " +
            "WHERE c.eliminado = false " +
            "AND (:nivel IS NULL OR c.nivel = :nivel) " +
            "AND (:area IS NULL OR :area = '' OR ar = :area) " +
            "AND (:q IS NULL OR :q = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Curso> buscar(@Param("q") String q, @Param("nivel") Nivel nivel, @Param("area") String area, Pageable pageable);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.eliminado = false")
    List<Curso> findAllConProfesor();

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.eliminado = false AND c.nivel = :nivel")
    List<Curso> findAllConProfesorByNivel(@Param("nivel") Nivel nivel);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id = :id")
    Optional<Curso> findByIdConProfesor(Long id);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.id IN :ids")
    List<Curso> findAllByIdConProfesor(List<Long> ids);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.profesor JOIN c.areas a " +
            "WHERE a = :area AND c.nivel = :nivel AND c.eliminado = false")
    List<Curso> findByNivelAndArea(@Param("nivel") Nivel nivel, @Param("area") String area);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.profesor.id = :profesorId AND c.eliminado = false")
    List<Curso> findByProfesorIdAndEliminadoFalse(@Param("profesorId") Long profesorId);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.profesor WHERE c.destacadoWeb = true AND c.eliminado = false")
    List<Curso> findByDestacadoWebTrueAndEliminadoFalse();
}