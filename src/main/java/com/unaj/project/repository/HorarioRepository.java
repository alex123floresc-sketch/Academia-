// src/main/java/com/unaj/project/repository/HorarioRepository.java
package com.unaj.project.repository;

import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    @Query("SELECT h FROM Horario h " +
            "JOIN FETCH h.curso c LEFT JOIN FETCH c.profesor " +
            "WHERE h.ciclo.id = :cicloId AND h.turno = :turno " +
            "ORDER BY h.diaSemana ASC, h.horaInicio ASC")
    List<Horario> findParaGrilla(Long cicloId, Turno turno);

    // Para validar cruces de hora dentro del mismo ciclo + turno + día
    List<Horario> findByCicloIdAndTurnoAndDiaSemana(Long cicloId, Turno turno, DiaSemana diaSemana);
}