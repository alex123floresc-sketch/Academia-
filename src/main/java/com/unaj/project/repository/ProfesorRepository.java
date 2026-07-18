package com.unaj.project.repository;

import com.unaj.project.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    List<Profesor> findByEliminadoFalse();
}