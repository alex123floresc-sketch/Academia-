package com.unaj.project.repository;

import com.unaj.project.model.LogroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogroIngresoRepository extends JpaRepository<LogroIngreso, Long> {

    List<LogroIngreso> findAllByOrderByOrdenAscIdAsc();

    List<LogroIngreso> findByActivoTrueOrderByOrdenAscIdAsc();
}
