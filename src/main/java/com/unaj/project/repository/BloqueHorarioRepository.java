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

    /** Igual que findByCicloIdAndNivelAndAreaOrderByHoraInicioAsc pero además exige coincidencia exacta
     * de salón: si salonId es null, solo trae bloques sin salón asignado; si no, solo los de ese salón. */
    @Query("SELECT b FROM BloqueHorario b WHERE b.ciclo.id = :cicloId AND b.nivel = :nivel AND b.area = :area " +
            "AND ((:salonId IS NULL AND b.salon IS NULL) OR (:salonId IS NOT NULL AND b.salon.id = :salonId)) " +
            "ORDER BY b.horaInicio ASC")
    List<BloqueHorario> findByCicloIdAndNivelAndAreaAndSalonOrderByHoraInicioAsc(
            @Param("cicloId") Long cicloId, @Param("nivel") Nivel nivel, @Param("area") String area, @Param("salonId") Long salonId);

    @Query("SELECT COUNT(b) > 0 FROM BloqueHorario b WHERE b.ciclo.id = :cicloId AND b.nivel = :nivel " +
            "AND b.turno = :turno AND b.horaInicio = :horaInicio AND b.area = :area " +
            "AND ((:salonId IS NULL AND b.salon IS NULL) OR (:salonId IS NOT NULL AND b.salon.id = :salonId))")
    boolean existsByCicloIdAndNivelAndTurnoAndHoraInicioAndAreaAndSalon(
            @Param("cicloId") Long cicloId, @Param("nivel") Nivel nivel, @Param("turno") Turno turno,
            @Param("horaInicio") LocalTime horaInicio, @Param("area") String area, @Param("salonId") Long salonId);

    @Modifying
    @Query("UPDATE BloqueHorario b SET b.nivel = :nivel WHERE b.nivel IS NULL")
    int backfillNivel(@Param("nivel") Nivel nivel);
}
