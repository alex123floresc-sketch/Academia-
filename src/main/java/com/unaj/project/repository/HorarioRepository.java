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
            "WHERE h.jornada.ciclo.id = :cicloId AND h.jornada.turno = :turno AND h.jornada.diaSemana = :diaSemana")
    List<Horario> findByCicloIdAndTurnoAndDiaSemana(@Param("cicloId") Long cicloId, @Param("turno") Turno turno,
                                                     @Param("diaSemana") DiaSemana diaSemana);

    boolean existsByJornadaIdAndCursoId(Long jornadaId, Long cursoId);
}
