package com.unaj.project.repository;

import com.unaj.project.model.PagoProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoProfesorRepository extends JpaRepository<PagoProfesor, Long> {

    List<PagoProfesor> findByProfesorIdOrderByFechaPagoDesc(Long profesorId);
}
