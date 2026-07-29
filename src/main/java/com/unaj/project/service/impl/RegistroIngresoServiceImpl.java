package com.unaj.project.service.impl;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.RegistroIngreso;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.RegistroIngresoRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.RegistroIngresoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistroIngresoServiceImpl implements RegistroIngresoService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistroIngresoRepository registroIngresoRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioRepository usuarioRepository;

    public RegistroIngresoServiceImpl(RegistroIngresoRepository registroIngresoRepository,
                                      AlumnoRepository alumnoRepository,
                                      UsuarioRepository usuarioRepository) {
        this.registroIngresoRepository = registroIngresoRepository;
        this.alumnoRepository = alumnoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<RegistroIngreso> listarDeHoy() {
        return registroIngresoRepository.findByFechaConAlumno(LocalDate.now());
    }

    @Override
    public long contarDeHoy() {
        return registroIngresoRepository.countByFecha(LocalDate.now());
    }

    @Override
    @Transactional
    public AsistenciaResultadoDTO registrar(String codigo, String username) {
        Alumno alumno = AlumnoCodigoResolver.resolver(alumnoRepository, codigo);
        if (alumno == null || alumno.isEliminado()) {
            return new AsistenciaResultadoDTO(false, "No se encontró ningún alumno con ese código o DNI.", null);
        }

        LocalDate hoy = LocalDate.now();
        if (registroIngresoRepository.existsByAlumnoIdAndFecha(alumno.getId(), hoy)) {
            return new AsistenciaResultadoDTO(false,
                    alumno.getNombreCompleto() + " ya tiene su ingreso registrado hoy.", alumno.getNombreCompleto());
        }

        Usuario registradoPor = (username != null) ? usuarioRepository.findByUsername(username) : null;

        RegistroIngreso registro = new RegistroIngreso();
        registro.setAlumno(alumno);
        registro.setFecha(hoy);
        registro.setHoraIngreso(LocalDateTime.now());
        registro.setRegistradoPor(registradoPor);
        registroIngresoRepository.save(registro);

        return new AsistenciaResultadoDTO(true,
                "Ingreso registrado a las " + registro.getHoraIngreso().format(HORA) + ".",
                alumno.getNombreCompleto());
    }
}
