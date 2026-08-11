package com.unaj.project.repository;

import com.unaj.project.model.FotoGaleria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoGaleriaRepository extends JpaRepository<FotoGaleria, Long> {

    List<FotoGaleria> findAllByOrderByOrdenAscIdAsc();

    List<FotoGaleria> findByActivaTrueOrderByOrdenAscIdAsc();
}
