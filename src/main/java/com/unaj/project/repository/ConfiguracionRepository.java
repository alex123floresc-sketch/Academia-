package com.unaj.project.repository;

import com.unaj.project.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    @Modifying
    @Query("UPDATE Configuracion c SET c.mostrarProcesoAdmision = true, c.mostrarFaq = true, " +
            "c.mostrarTestimonios = true, c.mostrarLogros = true, c.mostrarGaleria = true, " +
            "c.mostrarCalendario = true, c.mostrarFormularioContacto = true " +
            "WHERE c.mostrarProcesoAdmision = false AND c.mostrarFaq = false AND c.mostrarTestimonios = false " +
            "AND c.mostrarLogros = false AND c.mostrarGaleria = false AND c.mostrarCalendario = false " +
            "AND c.mostrarFormularioContacto = false")
    int backfillMostrarPaginaWebSiTodoApagado();
}
