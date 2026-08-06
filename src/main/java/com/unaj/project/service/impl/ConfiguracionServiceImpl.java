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
    private static final int NUMERO_CUOTAS_INICIAL = 1;
    private static final int DIAS_ENTRE_CUOTAS_INICIAL = 30;
    private static final int DIAS_GRACIA_INICIAL = 0;

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionServiceImpl(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    @Transactional
    public Configuracion obtener() {
        Configuracion configuracion = configuracionRepository.findById(ID).orElseGet(() -> {
            Configuracion nueva = new Configuracion();
            nueva.setMontoMatricula(MONTO_MATRICULA_INICIAL);
            nueva.setMontoPension(MONTO_PENSION_INICIAL);
            return nueva;
        });

        boolean incompleta = false;
        if (configuracion.getNumeroCuotasPension() == null) {
            configuracion.setNumeroCuotasPension(NUMERO_CUOTAS_INICIAL);
            incompleta = true;
        }
        if (configuracion.getDiasEntreCuotas() == null) {
            configuracion.setDiasEntreCuotas(DIAS_ENTRE_CUOTAS_INICIAL);
            incompleta = true;
        }
        if (configuracion.getDiasGraciaVencimiento() == null) {
            configuracion.setDiasGraciaVencimiento(DIAS_GRACIA_INICIAL);
            incompleta = true;
        }
        if (configuracion.getId() == null || incompleta) {
            configuracion = configuracionRepository.save(configuracion);
        }
        return configuracion;
    }

    @Override
    @Transactional
    public void actualizar(BigDecimal montoMatricula, BigDecimal montoPension,
                           Integer numeroCuotasPension, Integer diasEntreCuotas, Integer diasGraciaVencimiento) {
        if (montoMatricula == null || montoMatricula.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de matrícula debe ser mayor a 0.");
        }
        if (montoPension == null || montoPension.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de pensión no puede ser negativo.");
        }
        if (numeroCuotasPension == null || numeroCuotasPension < 1) {
            throw new IllegalArgumentException("El número de cuotas de pensión debe ser al menos 1.");
        }
        if (diasEntreCuotas == null || diasEntreCuotas < 1) {
            throw new IllegalArgumentException("Los días entre cuotas deben ser al menos 1.");
        }
        if (diasGraciaVencimiento == null || diasGraciaVencimiento < 0) {
            throw new IllegalArgumentException("Los días de gracia no pueden ser negativos.");
        }
        Configuracion configuracion = obtener();
        configuracion.setMontoMatricula(montoMatricula);
        configuracion.setMontoPension(montoPension);
        configuracion.setNumeroCuotasPension(numeroCuotasPension);
        configuracion.setDiasEntreCuotas(diasEntreCuotas);
        configuracion.setDiasGraciaVencimiento(diasGraciaVencimiento);
        configuracionRepository.save(configuracion);
    }
}
