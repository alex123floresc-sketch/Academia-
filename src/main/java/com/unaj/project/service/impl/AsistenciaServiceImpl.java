package com.unaj.project.service.impl;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Asistencia;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.AsistenciaRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.repository.MatriculaDetalleRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.AsistenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final HorarioRepository horarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final MatriculaDetalleRepository matriculaDetalleRepository;
    private final UsuarioRepository usuarioRepository;

    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository,
                                 HorarioRepository horarioRepository,
                                 AlumnoRepository alumnoRepository,
                                 MatriculaDetalleRepository matriculaDetalleRepository,
                                 UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.horarioRepository = horarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.matriculaDetalleRepository = matriculaDetalleRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Asistencia> listarDeHoy(Long horarioId) {
        return asistenciaRepository.findByHorarioIdAndFechaConAlumno(horarioId, LocalDate.now());
    }

    @Override
    public long contarDeHoy(Long horarioId) {
        return asistenciaRepository.countByHorarioIdAndFecha(horarioId, LocalDate.now());
    }

    @Override
    @Transactional
    public AsistenciaResultadoDTO registrar(Long horarioId, String codigoQr, String username) {
        Horario horario = horarioRepository.findById(horarioId).orElse(null);
        if (horario == null) {
            return new AsistenciaResultadoDTO(false, "La sesión de clase no existe.", null);
        }

        Long alumnoId = parseCodigoQr(codigoQr);
        if (alumnoId == null) {
            return new AsistenciaResultadoDTO(false, "Código QR no reconocido.", null);
        }

        Alumno alumno = alumnoRepository.findById(alumnoId).orElse(null);
        if (alumno == null || alumno.isEliminado()) {
            return new AsistenciaResultadoDTO(false, "Alumno no encontrado.", null);
        }

        boolean matriculado = matriculaDetalleRepository.existeMatriculaActiva(
                alumno.getId(), horario.getCurso().getId(), horario.getCiclo().getId(), horario.getTurno());
        if (!matriculado) {
            return new AsistenciaResultadoDTO(false,
                    alumno.getNombreCompleto() + " no está matriculado en este curso.", alumno.getNombreCompleto());
        }

        LocalDate hoy = LocalDate.now();
        if (asistenciaRepository.existsByAlumnoIdAndHorarioIdAndFecha(alumno.getId(), horarioId, hoy)) {
            return new AsistenciaResultadoDTO(false,
                    alumno.getNombreCompleto() + " ya tiene asistencia registrada hoy.", alumno.getNombreCompleto());
        }

        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        Asistencia asistencia = new Asistencia();
        asistencia.setAlumno(alumno);
        asistencia.setHorario(horario);
        asistencia.setFecha(hoy);
        asistencia.setHoraRegistro(LocalDateTime.now());
        asistencia.setRegistradoPor(registradoPor);
        asistenciaRepository.save(asistencia);

        return new AsistenciaResultadoDTO(true, "Asistencia registrada.", alumno.getNombreCompleto());
    }

    private Long parseCodigoQr(String codigo) {
        if (codigo == null) return null;
        String limpio = codigo.trim();
        if (limpio.startsWith("ALU-")) {
            limpio = limpio.substring(4);
        }
        try {
            return Long.parseLong(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
