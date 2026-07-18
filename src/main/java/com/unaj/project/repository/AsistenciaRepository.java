package com.unaj.project.repository;

import com.unaj.project.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByAlumnoIdAndHorarioIdAndFecha(Long alumnoId, Long horarioId, LocalDate fecha);

    long countByHorarioIdAndFecha(Long horarioId, LocalDate fecha);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.alumno " +
            "WHERE a.horario.id = :horarioId AND a.fecha = :fecha " +
            "ORDER BY a.horaRegistro DESC")
    List<Asistencia> findByHorarioIdAndFechaConAlumno(Long horarioId, LocalDate fecha);
}
