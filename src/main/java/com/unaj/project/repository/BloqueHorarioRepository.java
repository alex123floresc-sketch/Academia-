package com.unaj.project.repository;

import com.unaj.project.model.BloqueHorario;
import com.unaj.project.model.Nivel;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {

    List<BloqueHorario> findByCicloIdAndNivelAndAreaOrderByHoraInicioAsc(Long cicloId, Nivel nivel, String area);

    List<BloqueHorario> findByCicloId(Long cicloId);

    boolean existsByCicloIdAndNivelAndTurnoAndHoraInicioAndArea(
            Long cicloId, Nivel nivel, Turno turno, LocalTime horaInicio, String area);

    @Modifying
    @Query("UPDATE BloqueHorario b SET b.nivel = :nivel WHERE b.nivel IS NULL")
    int backfillNivel(@Param("nivel") Nivel nivel);
}
