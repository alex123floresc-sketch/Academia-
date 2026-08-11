package com.unaj.project.repository;

import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    @Query("SELECT h FROM Horario h " +
            "JOIN FETCH h.curso c LEFT JOIN FETCH c.profesor " +
            "WHERE h.bloque.ciclo.id = :cicloId AND h.bloque.turno = :turno AND h.diaSemana = :diaSemana")
    List<Horario> findByCicloIdAndTurnoAndDiaSemana(@Param("cicloId") Long cicloId, @Param("turno") Turno turno,
                                                     @Param("diaSemana") DiaSemana diaSemana);

    @Query("SELECT h FROM Horario h JOIN FETCH h.curso c LEFT JOIN FETCH c.profesor " +
            "WHERE h.bloque.ciclo.id = :cicloId")
    List<Horario> findByCicloId(@Param("cicloId") Long cicloId);

    @Query("SELECT h FROM Horario h JOIN FETCH h.curso c LEFT JOIN FETCH c.profesor " +
            "WHERE h.bloque.id = :bloqueId AND h.diaSemana = :dia")
    List<Horario> findByBloqueIdAndDiaSemana(@Param("bloqueId") Long bloqueId, @Param("dia") DiaSemana dia);

    boolean existsByBloqueIdAndDiaSemanaAndCursoId(Long bloqueId, DiaSemana diaSemana, Long cursoId);

    @Query("SELECT h FROM Horario h JOIN FETCH h.curso c JOIN FETCH h.bloque b " +
            "WHERE c.profesor.id = :profesorId AND c.eliminado = false")
    List<Horario> findByCursoProfesorId(@Param("profesorId") Long profesorId);

    @Query("SELECT h FROM Horario h JOIN FETCH h.curso c JOIN FETCH h.bloque b " +
            "WHERE c.id = :cursoId AND b.ciclo.id = :cicloId AND b.turno = :turno")
    List<Horario> findByCursoIdAndCicloIdAndTurno(@Param("cursoId") Long cursoId, @Param("cicloId") Long cicloId,
                                                   @Param("turno") Turno turno);
}
