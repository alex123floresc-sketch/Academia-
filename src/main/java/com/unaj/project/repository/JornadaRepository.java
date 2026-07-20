package com.unaj.project.repository;

import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Jornada;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JornadaRepository extends JpaRepository<Jornada, Long> {

    Optional<Jornada> findByCicloIdAndDiaSemanaAndTurno(Long cicloId, DiaSemana diaSemana, Turno turno);

    // Todas las jornadas de un ciclo con sus cursos ya cargados, para pintar la grilla completa
    @Query("SELECT DISTINCT j FROM Jornada j " +
            "LEFT JOIN FETCH j.horarios h LEFT JOIN FETCH h.curso c LEFT JOIN FETCH c.profesor " +
            "WHERE j.ciclo.id = :cicloId")
    List<Jornada> findParaGrilla(@Param("cicloId") Long cicloId);
}
