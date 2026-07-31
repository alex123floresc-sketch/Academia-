package com.unaj.project.service.impl;

import com.unaj.project.model.Curso;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.RegistroHoras;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RegistroHorasRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.RegistroHorasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistroHorasServiceImpl implements RegistroHorasService {

    private final RegistroHorasRepository registroHorasRepository;
    private final HorarioService horarioService;
    private final UsuarioRepository usuarioRepository;

    public RegistroHorasServiceImpl(RegistroHorasRepository registroHorasRepository,
                                    HorarioService horarioService,
                                    UsuarioRepository usuarioRepository) {
        this.registroHorasRepository = registroHorasRepository;
        this.horarioService = horarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public RegistroHoras registrar(Long horarioId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                   String observaciones, String username) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (registroHorasRepository.existsByHorarioIdAndFecha(horarioId, fecha)) {
            throw new IllegalStateException("Ya existe un registro de horas para esa clase en esa fecha.");
        }

        Horario horario = horarioService.buscarPorId(horarioId);
        Curso curso = horario.getCurso();
        Profesor profesor = curso.getProfesor();
        if (profesor == null) {
            throw new IllegalStateException("El curso \"" + curso.getNombre() + "\" no tiene profesor asignado.");
        }

        BigDecimal horas = BigDecimal.valueOf(Duration.between(horaInicio, horaFin).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        RegistroHoras registro = new RegistroHoras();
        registro.setProfesor(profesor);
        registro.setHorario(horario);
        registro.setFecha(fecha);
        registro.setHoraInicio(horaInicio);
        registro.setHoraFin(horaFin);
        registro.setHoras(horas);
        registro.setObservaciones(observaciones);
        registro.setRegistradoPor(registradoPor);
        registro.setCreadoEn(LocalDateTime.now());
        return registroHorasRepository.save(registro);
    }

    @Override
    public List<RegistroHoras> listarPorProfesor(Long profesorId) {
        return registroHorasRepository.findByProfesorIdOrderByFechaDesc(profesorId);
    }

    @Override
    public List<RegistroHoras> listarPorProfesorEnRango(Long profesorId, LocalDate desde, LocalDate hasta) {
        return registroHorasRepository.findByProfesorIdAndFechaBetween(profesorId, desde, hasta);
    }
}
