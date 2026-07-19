package com.unaj.project.repository;

import com.unaj.project.model.Abono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonoRepository extends JpaRepository<Abono, Long> {

    @Query("SELECT a FROM Abono a JOIN FETCH a.pago WHERE a.pago.id = :pagoId ORDER BY a.fecha DESC")
    List<Abono> findByPagoIdOrderByFechaDesc(Long pagoId);

    // Reporte: ingresos reales cobrados agrupados por mes (yyyy-MM), basado en abonos efectivamente recibidos
    @Query(value = "SELECT DATE_FORMAT(a.fecha, '%Y-%m'), SUM(a.monto) " +
            "FROM abonos a GROUP BY DATE_FORMAT(a.fecha, '%Y-%m') ORDER BY 1", nativeQuery = true)
    List<Object[]> sumarPorMes();
}
