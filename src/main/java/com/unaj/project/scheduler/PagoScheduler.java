package com.unaj.project.scheduler;

import com.unaj.project.service.PagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PagoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PagoScheduler.class);

    private final PagoService pagoService;

    public PagoScheduler(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Todos los días a la 1:00 a.m., marca como VENCIDO los pagos PENDIENTE ya vencidos
    @Scheduled(cron = "0 0 1 * * *")
    public void marcarPagosVencidos() {
        int actualizados = pagoService.marcarVencidos();
        if (actualizados > 0) {
            log.info("Pagos marcados como VENCIDO: {}", actualizados);
        }
    }
}
