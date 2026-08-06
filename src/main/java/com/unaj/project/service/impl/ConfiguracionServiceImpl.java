package com.unaj.project.service.impl;

import com.unaj.project.model.Configuracion;
import com.unaj.project.repository.ConfiguracionRepository;
import com.unaj.project.service.ConfiguracionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private static final Long ID = 1L;
    private static final BigDecimal MONTO_MATRICULA_INICIAL = new BigDecimal("150.00");
    private static final BigDecimal MONTO_PENSION_INICIAL = new BigDecimal("180.00");

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionServiceImpl(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    @Transactional
    public Configuracion obtener() {
        return configuracionRepository.findById(ID).orElseGet(() -> {
            Configuracion configuracion = new Configuracion();
            configuracion.setMontoMatricula(MONTO_MATRICULA_INICIAL);
            configuracion.setMontoPension(MONTO_PENSION_INICIAL);
            return configuracionRepository.save(configuracion);
        });
    }

    @Override
    @Transactional
    public void actualizar(BigDecimal montoMatricula, BigDecimal montoPension) {
        if (montoMatricula == null || montoMatricula.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de matrícula debe ser mayor a 0.");
        }
        if (montoPension == null || montoPension.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de pensión no puede ser negativo.");
        }
        Configuracion configuracion = obtener();
        configuracion.setMontoMatricula(montoMatricula);
        configuracion.setMontoPension(montoPension);
        configuracionRepository.save(configuracion);
    }
}
