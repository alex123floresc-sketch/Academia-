package com.unaj.project.repository;

import com.unaj.project.model.PreguntaFrecuente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaFrecuenteRepository extends JpaRepository<PreguntaFrecuente, Long> {

    List<PreguntaFrecuente> findAllByOrderByOrdenAscIdAsc();

    List<PreguntaFrecuente> findByActivaTrueOrderByOrdenAscIdAsc();
}
