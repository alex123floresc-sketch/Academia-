package com.unaj.project.repository;

import com.unaj.project.model.Alumno;
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

    // Alumnos que tienen al menos un pago que coincide con la búsqueda (para agrupar el listado por alumno)
    @Query(value = "SELECT DISTINCT m.estudiante FROM Pago p JOIN p.matricula m JOIN m.estudiante e " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.concepto) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY e.apellido, e.nombre",
            countQuery = "SELECT COUNT(DISTINCT m.estudiante) FROM Pago p JOIN p.matricula m JOIN m.estudiante e " +
            "WHERE (:q IS NULL OR :q = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.concepto) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Alumno> buscarAlumnosConPagos(@Param("q") String q, Pageable pageable);

    // Todos los pagos (con concepto coincidente si se buscó por concepto) de un grupo de alumnos, para pintar cada tarjeta
    @Query("SELECT p FROM Pago p JOIN FETCH p.matricula m JOIN FETCH m.estudiante e " +
            "WHERE e.id IN :alumnoIds " +
            "ORDER BY e.apellido, e.nombre, p.fechaVencimiento DESC")
    List<Pago> buscarPorAlumnoIds(@Param("alumnoIds") List<Long> alumnoIds);

    // Cuenta pagos no pagados agrupados por alumno (id del alumno, cantidad)
    @Query("SELECT p.matricula.estudiante.id, COUNT(p) " +
            "FROM Pago p WHERE p.estado <> 'PAGADO' " +
            "GROUP BY p.matricula.estudiante.id")
    List<Object[]> contarDeudaPorAlumno();

    // Reporte: alumnos con pagos vencidos, cantidad y saldo realmente adeudado
    @Query("SELECT a.id, a.nombre, a.apellido, a.email, COUNT(p), SUM(p.monto - COALESCE(p.montoPagado, 0)) " +
            "FROM Pago p JOIN p.matricula m JOIN m.estudiante a " +
            "WHERE p.estado = 'VENCIDO' " +
            "GROUP BY a.id, a.nombre, a.apellido, a.email " +
            "ORDER BY SUM(p.monto - COALESCE(p.montoPagado, 0)) DESC")
    List<Object[]> listarMorosos();

    // Marca como VENCIDO todo pago PENDIENTE o PARCIAL cuya fecha de vencimiento ya pasó
    @Modifying
    @Query("UPDATE Pago p SET p.estado = 'VENCIDO' " +
            "WHERE p.estado IN ('PENDIENTE', 'PARCIAL') AND p.fechaVencimiento < :hoy")
    int marcarVencidos(@Param("hoy") LocalDate hoy);
}