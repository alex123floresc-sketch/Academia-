package com.unaj.project.repository;

import com.unaj.project.model.MatriculaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriculaDetalleRepository extends JpaRepository<MatriculaDetalle, Long> {

    @Modifying
    @Query("DELETE FROM MatriculaDetalle d WHERE d.matricula.id = :matriculaId")
    void deleteByMatriculaId(Long matriculaId);
}