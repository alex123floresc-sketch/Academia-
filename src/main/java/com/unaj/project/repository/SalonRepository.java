package com.unaj.project.repository;

import com.unaj.project.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {

    List<Salon> findAllByOrderByNombreAsc();

    List<Salon> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByNombreIgnoreCase(String nombre);
}
