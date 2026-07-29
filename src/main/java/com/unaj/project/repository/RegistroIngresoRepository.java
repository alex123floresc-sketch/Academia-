package com.unaj.project.repository;

import com.unaj.project.model.RegistroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistroIngresoRepository extends JpaRepository<RegistroIngreso, Long> {

    boolean existsByAlumnoIdAndFecha(Long alumnoId, LocalDate fecha);

    long countByFecha(LocalDate fecha);

    @Query("SELECT r FROM RegistroIngreso r JOIN FETCH r.alumno " +
            "WHERE r.fecha = :fecha ORDER BY r.horaIngreso DESC")
    List<RegistroIngreso> findByFechaConAlumno(LocalDate fecha);
}
