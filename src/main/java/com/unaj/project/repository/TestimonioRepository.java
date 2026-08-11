package com.unaj.project.repository;

import com.unaj.project.model.Testimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonioRepository extends JpaRepository<Testimonio, Long> {

    List<Testimonio> findAllByOrderByOrdenAscIdAsc();

    List<Testimonio> findByActivoTrueOrderByOrdenAscIdAsc();
}
