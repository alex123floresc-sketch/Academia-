package com.unaj.project.repository;

import com.unaj.project.model.Ciclo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CicloRepository extends JpaRepository<Ciclo, Long> {

    // Solo el ciclo vigente (activo) que no esté eliminado
    Ciclo findFirstByActivoTrueAndEliminadoFalse();

    List<Ciclo> findByEliminadoFalse();
}