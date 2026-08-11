package com.unaj.project.repository;

import com.unaj.project.model.PasoAdmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasoAdmisionRepository extends JpaRepository<PasoAdmision, Long> {

    List<PasoAdmision> findAllByOrderByOrdenAscIdAsc();

    List<PasoAdmision> findByActivoTrueOrderByOrdenAscIdAsc();

    long countBy();
}
