package com.unaj.project.repository;

import com.unaj.project.model.SolicitudEliminacionUsuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudEliminacionUsuarioRepository extends JpaRepository<SolicitudEliminacionUsuario, Long> {

    Optional<SolicitudEliminacionUsuario> findByObjetivoId(Long objetivoId);

    /**
     * Bloquea la fila mientras dure la transacción: si dos administradores aprueban la misma
     * solicitud casi al mismo tiempo, el segundo espera a que termine el primero en vez de que
     * ambos lean el mismo conteo de aprobaciones y se pisen entre sí.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SolicitudEliminacionUsuario s WHERE s.objetivo.id = :objetivoId")
    Optional<SolicitudEliminacionUsuario> findByObjetivoIdParaActualizar(@Param("objetivoId") Long objetivoId);

    @Query("SELECT s FROM SolicitudEliminacionUsuario s JOIN FETCH s.objetivo JOIN FETCH s.solicitadoPor")
    List<SolicitudEliminacionUsuario> findAllConDetalle();

    void deleteByObjetivoId(Long objetivoId);
}
