package com.unaj.project.repository;

import com.unaj.project.model.BloqueHorario;
import com.unaj.project.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {

    List<BloqueHorario> findByCicloIdAndAreaOrderByHoraInicioAsc(Long cicloId, String area);

    boolean existsByCicloIdAndTurnoAndHoraInicioAndArea(Long cicloId, Turno turno, LocalTime horaInicio, String area);
}
