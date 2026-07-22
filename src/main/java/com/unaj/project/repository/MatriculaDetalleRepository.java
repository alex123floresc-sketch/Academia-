package com.unaj.project.repository;

import com.unaj.project.model.MatriculaDetalle;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriculaDetalleRepository extends JpaRepository<MatriculaDetalle, Long> {

    @Modifying
    @Query("DELETE FROM MatriculaDetalle d WHERE d.matricula.id = :matriculaId")
    void deleteByMatriculaId(Long matriculaId);

    @Query("SELECT COUNT(d) > 0 FROM MatriculaDetalle d " +
            "WHERE d.curso.id = :cursoId AND d.matricula.semestre.id = :cicloId " +
            "AND d.matricula.turno = :turno AND d.matricula.estudiante.id = :alumnoId " +
            "AND d.matricula.estado = 'ACTIVA'")
    boolean existeMatriculaActiva(@Param("alumnoId") Long alumnoId, @Param("cursoId") Long cursoId,
                                   @Param("cicloId") Long cicloId, @Param("turno") Turno turno);
}