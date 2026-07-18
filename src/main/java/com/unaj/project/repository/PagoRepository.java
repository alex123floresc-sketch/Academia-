package com.unaj.project.repository;

import com.unaj.project.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByMatriculaId(Long matriculaId);

    List<Pago> findByEstado(String estado);

    // Pagos con su matrícula y alumno cargados, para el listado de pagos
    @Query("SELECT p FROM Pago p " +
            "JOIN FETCH p.matricula m " +
            "JOIN FETCH m.estudiante " +
            "ORDER BY p.fechaVencimiento DESC")
    List<Pago> findAllConAlumno();

    // Cuenta pagos no pagados agrupados por alumno (id del alumno, cantidad)
    @Query("SELECT p.matricula.estudiante.id, COUNT(p) " +
            "FROM Pago p WHERE p.estado <> 'PAGADO' " +
            "GROUP BY p.matricula.estudiante.id")
    List<Object[]> contarDeudaPorAlumno();
}