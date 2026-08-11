package com.unaj.project.repository;

import com.unaj.project.model.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Long> {

    boolean existsByHorarioIdAndFecha(Long horarioId, LocalDate fecha);

    Optional<RegistroHoras> findByHorarioIdAndFecha(Long horarioId, LocalDate fecha);

    List<RegistroHoras> findByHorarioIdInAndFecha(List<Long> horarioIds, LocalDate fecha);

    @Query("SELECT r FROM RegistroHoras r " +
            "JOIN FETCH r.horario h JOIN FETCH h.curso " +
            "WHERE r.profesor.id = :profesorId AND r.fecha BETWEEN :desde AND :hasta " +
            "ORDER BY r.fecha DESC")
    List<RegistroHoras> findByProfesorIdAndFechaBetween(@Param("profesorId") Long profesorId,
                                                         @Param("desde") LocalDate desde,
                                                         @Param("hasta") LocalDate hasta);

    @Query("SELECT r FROM RegistroHoras r " +
            "JOIN FETCH r.horario h JOIN FETCH h.curso " +
            "WHERE r.profesor.id = :profesorId ORDER BY r.fecha DESC")
    List<RegistroHoras> findByProfesorIdOrderByFechaDesc(@Param("profesorId") Long profesorId);
}
