package com.unaj.project.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class PeriodoUtil {

    private PeriodoUtil() {}

    public record Rango(LocalDate inicio, LocalDate fin) {}

    public static Rango semanaDe(LocalDate fecha) {
        LocalDate lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new Rango(lunes, lunes.plusDays(6));
    }

    public static Rango quincenaDe(LocalDate fecha) {
        if (fecha.getDayOfMonth() <= 15) {
            return new Rango(fecha.withDayOfMonth(1), fecha.withDayOfMonth(15));
        }
        return new Rango(fecha.withDayOfMonth(16), fecha.with(TemporalAdjusters.lastDayOfMonth()));
    }

    public static Rango semanaAnterior(Rango actual) { return semanaDe(actual.inicio().minusDays(7)); }
    public static Rango semanaSiguiente(Rango actual) { return semanaDe(actual.inicio().plusDays(7)); }

    public static Rango quincenaAnterior(Rango actual) { return quincenaDe(actual.inicio().minusDays(1)); }
    public static Rango quincenaSiguiente(Rango actual) { return quincenaDe(actual.fin().plusDays(1)); }
}
