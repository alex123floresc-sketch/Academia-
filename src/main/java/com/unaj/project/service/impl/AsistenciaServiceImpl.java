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

    // Este flujo registra únicamente la ENTRADA del alumno a la sesión (no hay registro de salida);
    // por eso basta con una fila por alumno+horario+día (ver existsByAlumnoIdAndHorarioIdAndFecha).
    @Override
    @Transactional
    public AsistenciaResultadoDTO registrar(Long horarioId, String codigoQr, String username) {
        Horario horario = horarioRepository.findById(horarioId).orElse(null);
        if (horario == null) {
            return new AsistenciaResultadoDTO(false, "La sesión de clase no existe.", null);
        }

        Alumno alumno = resolverAlumno(codigoQr);
        if (alumno == null || alumno.isEliminado()) {
            return new AsistenciaResultadoDTO(false, "No se encontró ningún alumno con ese código o DNI.", null);
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
                    alumno.getNombreCompleto() + " ya tiene su entrada registrada hoy.", alumno.getNombreCompleto());
        }

        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        Asistencia asistencia = new Asistencia();
        asistencia.setAlumno(alumno);
        asistencia.setHorario(horario);
        asistencia.setFecha(hoy);
        asistencia.setHoraRegistro(LocalDateTime.now());
        asistencia.setRegistradoPor(registradoPor);
        asistenciaRepository.save(asistencia);

        return new AsistenciaResultadoDTO(true, "Entrada registrada.", alumno.getNombreCompleto());
    }

    // Acepta el código del QR del carnet ("ALU-{id}") o, si el alumno no lo trajo, su DNI de 8 dígitos.
    private Alumno resolverAlumno(String codigo) {
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
