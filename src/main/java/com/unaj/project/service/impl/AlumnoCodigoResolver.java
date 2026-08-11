package com.unaj.project.service.impl;

import com.unaj.project.model.Alumno;
import com.unaj.project.repository.AlumnoRepository;

/** Resuelve el alumno detrás de un código de carnet (QR) o un DNI escrito a mano. */
final class AlumnoCodigoResolver {

    private AlumnoCodigoResolver() {}

    static Alumno resolver(AlumnoRepository alumnoRepository, String codigo) {
        if (codigo == null) return null;
        String limpio = codigo.trim();
        if (limpio.isEmpty()) return null;

        if (limpio.startsWith("ALU-")) {
            try {
                return alumnoRepository.findById(Long.parseLong(limpio.substring(4))).orElse(null);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (limpio.matches("\\d{8}")) {
            return alumnoRepository.findByDni(limpio).orElse(null);
        }

        try {
            return alumnoRepository.findById(Long.parseLong(limpio)).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
