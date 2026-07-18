package com.unaj.project.repository;

import com.unaj.project.model.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    // Búsqueda paginada por nombre/apellido del alumno o concepto del pago (q vacío = todos)
    @Query(value = "SELECT p FROM Pago p JOIN FETCH p.matricula m JOIN FETCH m.estudiante e " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.concepto) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY p.fechaVencimiento DESC",
            countQuery = "SELECT COUNT(p) FROM Pago p JOIN p.matricula m JOIN m.estudiante e " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.concepto) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Pago> buscar(@Param("q") String q, Pageable pageable);

    // Cuenta pagos no pagados agrupados por alumno (id del alumno, cantidad)
    @Query("SELECT p.matricula.estudiante.id, COUNT(p) " +
            "FROM Pago p WHERE p.estado <> 'PAGADO' " +
            "GROUP BY p.matricula.estudiante.id")
    List<Object[]> contarDeudaPorAlumno();

    // Reporte: ingresos cobrados agrupados por mes (yyyy-MM)
    @Query(value = "SELECT DATE_FORMAT(p.fecha_pago, '%Y-%m'), SUM(p.monto) " +
            "FROM pagos p WHERE p.estado = 'PAGADO' " +
            "GROUP BY DATE_FORMAT(p.fecha_pago, '%Y-%m') " +
            "ORDER BY 1", nativeQuery = true)
    List<Object[]> sumarIngresosPorMes();

    // Reporte: alumnos con pagos vencidos, cantidad y monto adeudado
    @Query("SELECT a.id, a.nombre, a.apellido, a.email, COUNT(p), SUM(p.monto) " +
            "FROM Pago p JOIN p.matricula m JOIN m.estudiante a " +
            "WHERE p.estado = 'VENCIDO' " +
            "GROUP BY a.id, a.nombre, a.apellido, a.email " +
            "ORDER BY SUM(p.monto) DESC")
    List<Object[]> listarMorosos();

    // Marca como VENCIDO todo pago PENDIENTE cuya fecha de vencimiento ya pasó
    @Modifying
    @Query("UPDATE Pago p SET p.estado = 'VENCIDO' " +
            "WHERE p.estado = 'PENDIENTE' AND p.fechaVencimiento < :hoy")
    int marcarVencidos(@Param("hoy") LocalDate hoy);
}