package com.unaj.project.repository;

import com.unaj.project.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String username);

    // Búsqueda paginada por usuario o nombre (q vacío = todos)
    @Query(value = "SELECT u FROM Usuario u WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')))",
            countQuery = "SELECT COUNT(u) FROM Usuario u WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Usuario> buscar(@Param("q") String q, Pageable pageable);
}