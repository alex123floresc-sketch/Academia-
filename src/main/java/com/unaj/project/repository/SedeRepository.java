package com.unaj.project.repository;

import com.unaj.project.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {

    List<Sede> findAllByOrderByNombreAsc();

    List<Sede> findByActivaTrueOrderByNombreAsc();
}
