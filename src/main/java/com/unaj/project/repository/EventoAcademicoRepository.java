package com.unaj.project.repository;

import com.unaj.project.model.EventoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoAcademicoRepository extends JpaRepository<EventoAcademico, Long> {

    List<EventoAcademico> findAllByOrderByFechaAsc();

    List<EventoAcademico> findByActivoTrueAndFechaGreaterThanEqualOrderByFechaAsc(LocalDate desde);
}
