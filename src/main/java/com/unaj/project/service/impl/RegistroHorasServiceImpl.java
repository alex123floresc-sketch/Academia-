package com.unaj.project.service.impl;

import com.unaj.project.model.Curso;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Profesor;
import com.unaj.project.model.RegistroHoras;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.RegistroHorasRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.RegistroHorasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistroHorasServiceImpl implements RegistroHorasService {

    private final RegistroHorasRepository registroHorasRepository;
    private final HorarioService horarioService;
    private final UsuarioRepository usuarioRepository;
    private final RegistroActividadService registroActividadService;
    private final ConfiguracionService configuracionService;

    public RegistroHorasServiceImpl(RegistroHorasRepository registroHorasRepository,
                                    HorarioService horarioService,
                                    UsuarioRepository usuarioRepository,
                                    RegistroActividadService registroActividadService,
                                    ConfiguracionService configuracionService) {
        this.registroHorasRepository = registroHorasRepository;
        this.horarioService = horarioService;
        this.usuarioRepository = usuarioRepository;
        this.registroActividadService = registroActividadService;
        this.configuracionService = configuracionService;
    }

    @Override
    @Transactional
    public RegistroHoras marcarLlegada(Long horarioId, LocalDate fecha, String username) {
        Horario horario = horarioService.buscarPorId(horarioId);
        Profesor profesor = resolverProfesor(horario);
        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        RegistroHoras registro = registroHorasRepository.findByHorarioIdAndFecha(horarioId, fecha)
                .orElseGet(() -> {
                    RegistroHoras nuevo = new RegistroHoras();
                    nuevo.setProfesor(profesor);
                    nuevo.setHorario(horario);
                    nuevo.setFecha(fecha);
                    nuevo.setCreadoEn(LocalDateTime.now());
                    return nuevo;
                });
        registro.setHoraLlegada(LocalDateTime.now());
        registro.setRegistradoPor(registradoPor);
        RegistroHoras guardado = registroHorasRepository.save(registro);
        registroActividadService.registrar(username, TipoAccion.CREAR, "Horas docentes", guardado.getId(),
                "Marcó la llegada de " + profesor.getNombre() + " " + profesor.getApellido());
        return guardado;
    }

    @Override
    @Transactional
    public RegistroHoras registrarHoras(Long horarioId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                        String observaciones, String username) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        Horario horario = horarioService.buscarPorId(horarioId);
        validarContraHorario(horario, fecha, horaInicio, horaFin);
        Profesor profesor = resolverProfesor(horario);
        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        BigDecimal horas = BigDecimal.valueOf(Duration.between(horaInicio, horaFin).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        RegistroHoras registro = registroHorasRepository.findByHorarioIdAndFecha(horarioId, fecha)
                .orElseGet(() -> {
                    RegistroHoras nuevo = new RegistroHoras();
                    nuevo.setProfesor(profesor);
                    nuevo.setHorario(horario);
                    nuevo.setFecha(fecha);
                    nuevo.setCreadoEn(LocalDateTime.now());
                    return nuevo;
                });
        registro.setHoraInicio(horaInicio);
        registro.setHoraFin(horaFin);
        registro.setHoras(horas);
        registro.setObservaciones(observaciones);
        registro.setRegistradoPor(registradoPor);
        RegistroHoras guardado = registroHorasRepository.save(registro);
        registroActividadService.registrar(username, TipoAccion.EDITAR, "Horas docentes", guardado.getId(),
                "Registró " + horas + " horas dictadas de " + profesor.getNombre() + " " + profesor.getApellido());
        return guardado;
    }

    @Override
    public List<RegistroHoras> listarPorProfesor(Long profesorId) {
        return registroHorasRepository.findByProfesorIdOrderByFechaDesc(profesorId);
    }

    @Override
    public List<RegistroHoras> listarPorProfesorEnRango(Long profesorId, LocalDate desde, LocalDate hasta) {
        return registroHorasRepository.findByProfesorIdAndFechaBetween(profesorId, desde, hasta);
    }

    @Override
    public Map<Long, RegistroHoras> buscarDeHoyPorHorarios(List<Long> horarioIds) {
        Map<Long, RegistroHoras> porHorario = new LinkedHashMap<>();
        if (horarioIds.isEmpty()) {
            return porHorario;
        }
        for (RegistroHoras r : registroHorasRepository.findByHorarioIdInAndFecha(horarioIds, LocalDate.now())) {
            porHorario.put(r.getHorario().getId(), r);
        }
        return porHorario;
    }

    private void validarContraHorario(Horario horario, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        DiaSemana diaFecha = DiaSemana.desde(fecha.getDayOfWeek());
        if (diaFecha != horario.getDiaSemana()) {
            String etiquetaFecha = diaFecha != null ? diaFecha.getEtiqueta() : "Domingo";
            throw new IllegalArgumentException("La fecha elegida cae en " + etiquetaFecha
                    + ", pero este curso está programado los " + horario.getDiaSemana().getEtiqueta()
                    + ". Elige una fecha que coincida con el día de clase.");
        }

        LocalTime inicioProgramado = horario.getHoraInicio();
        LocalTime finProgramado = horario.getHoraFin();
        if (inicioProgramado != null && finProgramado != null) {
            int toleranciaMinutos = configuracionService.obtener().getToleranciaMinutosHorasDocentes();
            LocalTime limiteInicio = inicioProgramado.minusMinutes(toleranciaMinutos);
            LocalTime limiteFin = finProgramado.plusMinutes(toleranciaMinutos);
            if (horaInicio.isBefore(limiteInicio) || horaFin.isAfter(limiteFin)) {
                throw new IllegalArgumentException("Las horas deben estar dentro del horario programado ("
                        + inicioProgramado + " - " + finProgramado + "), con un margen de "
                        + toleranciaMinutos + " minutos.");
            }
        }
    }

    private Profesor resolverProfesor(Horario horario) {
        Curso curso = horario.getCurso();
        Profesor profesor = curso.getProfesor();
        if (profesor == null) {
            throw new IllegalStateException("El curso \"" + curso.getNombre() + "\" no tiene profesor asignado.");
        }
        return profesor;
    }
}
