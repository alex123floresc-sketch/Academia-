package com.unaj.project.repository;

import com.unaj.project.model.SolicitudInformacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudInformacionRepository extends JpaRepository<SolicitudInformacion, Long> {

    List<SolicitudInformacion> findAllByOrderByFechaDesc();

    long countByAtendidaFalse();
}
