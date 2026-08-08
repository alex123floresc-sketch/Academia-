package com.unaj.project.model;

import java.util.List;
import java.util.Map;

public final class Areas {

    /** Áreas de Preuniversitario. Mantiene el nombre histórico "TODAS" — no renombrar, se usa en todo el módulo de Preuniversitario. */
    public static final List<String> TODAS = List.of("Ingenierías", "Biomédicas", "Sociales");

    public static final List<String> PRIMARIA = List.of(
            "1° grado", "2° grado", "3° grado", "4° grado", "5° grado", "6° grado");

    public static final List<String> SECUNDARIA = List.of(
            "1° grado", "2° grado", "3° grado", "4° grado", "5° grado");

    private static final Map<Nivel, List<String>> POR_NIVEL = Map.of(
            Nivel.PRIMARIA, PRIMARIA,
            Nivel.SECUNDARIA, SECUNDARIA,
            Nivel.PREUNIVERSITARIO, TODAS);

    /** Lista de clasificaciones (área o grado, según el nivel) válidas para ese nivel. */
    public static List<String> paraNivel(Nivel nivel) {
        return POR_NIVEL.get(nivel);
    }

    private Areas() {}
}
