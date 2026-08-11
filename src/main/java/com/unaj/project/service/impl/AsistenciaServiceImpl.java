package com.unaj.project.service.impl;

import com.unaj.project.dto.AsistenciaResultadoDTO;
import com.unaj.project.dto.AsistenciaResumenAlumnoDTO;
import com.unaj.project.dto.CursoAsistenciaResumenDTO;
import com.unaj.project.dto.DiaCalendarioAsistenciaDTO;
import com.unaj.project.dto.EstadoAsistenciaDia;
import com.unaj.project.dto.MesCalendarioAsistenciaDTO;
import com.unaj.project.model.Alumno;
import com.unaj.project.model.Asistencia;
import com.unaj.project.model.Ciclo;
import com.unaj.project.model.Curso;
import com.unaj.project.model.DiaSemana;
import com.unaj.project.model.Horario;
import com.unaj.project.model.Matricula;
import com.unaj.project.model.Usuario;
import com.unaj.project.repository.AlumnoRepository;
import com.unaj.project.repository.AsistenciaRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.repository.MatriculaDetalleRepository;
import com.unaj.project.repository.MatriculaRepository;
import com.unaj.project.repository.UsuarioRepository;
import com.unaj.project.service.AsistenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final HorarioRepository horarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final MatriculaDetalleRepository matriculaDetalleRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository,
                                 HorarioRepository horarioRepository,
                                 AlumnoRepository alumnoRepository,
                                 MatriculaDetalleRepository matriculaDetalleRepository,
                                 MatriculaRepository matriculaRepository,
                                 UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.horarioRepository = horarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.matriculaDetalleRepository = matriculaDetalleRepository;
        this.matriculaRepository = matriculaRepository;
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

    private Alumno resolverAlumno(String codigo) {
        return AlumnoCodigoResolver.resolver(alumnoRepository, codigo);
    }

    @Override
    public AsistenciaResumenAlumnoDTO resumenPorAlumno(Long alumnoId, Long cicloId) {
        List<Matricula> matriculas = matriculaRepository.findByEstudianteIdConDetalle(alumnoId);
        Matricula matricula = elegirMatricula(matriculas, cicloId);
        if (matricula == null) {
            return AsistenciaResumenAlumnoDTO.vacio(cicloId, null);
        }

        Ciclo ciclo = matricula.getSemestre();
        LocalDate hoy = LocalDate.now();
        LocalDate desde = maxFecha(ciclo.getFechaInicio(), matricula.getFechaMatricula().toLocalDate());
        LocalDate hasta = minFecha(ciclo.getFechaFin(), hoy);
        if (hasta.isBefore(desde)) {
            return AsistenciaResumenAlumnoDTO.vacio(ciclo.getId(), ciclo.getNombre());
        }

        List<Curso> cursos = matricula.getDetalles().stream().map(d -> d.getCurso()).toList();
        List<Horario> horarios = new ArrayList<>();
        for (Curso curso : cursos) {
            horarios.addAll(horarioRepository.findByCursoIdAndCicloIdAndTurno(curso.getId(), ciclo.getId(), matricula.getTurno()));
        }
        if (horarios.isEmpty()) {
            return AsistenciaResumenAlumnoDTO.vacio(ciclo.getId(), ciclo.getNombre());
        }

        List<Long> horarioIds = horarios.stream().map(Horario::getId).toList();
        List<Asistencia> asistencias = asistenciaRepository.findByAlumnoIdAndHorarioIdIn(alumnoId, horarioIds);
        Map<String, Asistencia> presentePorHorarioYFecha = new HashMap<>();
        for (Asistencia a : asistencias) {
            presentePorHorarioYFecha.put(a.getHorario().getId() + "_" + a.getFecha(), a);
        }

        Map<LocalDate, EstadoAsistenciaDia> estadoPorDia = new TreeMap<>();
        Map<Long, int[]> statsPorCurso = new LinkedHashMap<>();
        Map<Long, String> nombrePorCurso = new LinkedHashMap<>();
        int totalSesiones = 0, totalAsistencias = 0, totalFaltas = 0, totalTardanzas = 0;

        for (Horario horario : horarios) {
            Long cursoId = horario.getCurso().getId();
            nombrePorCurso.putIfAbsent(cursoId, horario.getCurso().getNombre());
            int[] stats = statsPorCurso.computeIfAbsent(cursoId, k -> new int[4]);

            for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
                if (DiaSemana.desde(fecha.getDayOfWeek()) != horario.getDiaSemana()) {
                    continue;
                }
                totalSesiones++;
                stats[0]++;

                Asistencia asistencia = presentePorHorarioYFecha.get(horario.getId() + "_" + fecha);
                EstadoAsistenciaDia estadoDia;
                if (asistencia != null) {
                    totalAsistencias++;
                    stats[1]++;
                    boolean tarde = horario.getHoraInicio() != null
                            && asistencia.getHoraRegistro().toLocalTime().isAfter(horario.getHoraInicio());
                    if (tarde) {
                        totalTardanzas++;
                        stats[3]++;
                        estadoDia = EstadoAsistenciaDia.TARDANZA;
                    } else {
                        estadoDia = EstadoAsistenciaDia.PRESENTE;
                    }
                } else {
                    totalFaltas++;
                    stats[2]++;
                    estadoDia = EstadoAsistenciaDia.FALTA;
                }

                estadoPorDia.merge(fecha, estadoDia, AsistenciaServiceImpl::peorEstado);
            }
        }

        List<CursoAsistenciaResumenDTO> porCurso = statsPorCurso.entrySet().stream()
                .map(e -> new CursoAsistenciaResumenDTO(nombrePorCurso.get(e.getKey()),
                        e.getValue()[0], e.getValue()[1], e.getValue()[2], e.getValue()[3]))
                .sorted(Comparator.comparing(CursoAsistenciaResumenDTO::cursoNombre))
                .toList();

        List<MesCalendarioAsistenciaDTO> meses = construirCalendario(desde, hasta, estadoPorDia);

        return new AsistenciaResumenAlumnoDTO(ciclo.getId(), ciclo.getNombre(), desde, hasta,
                totalSesiones, totalAsistencias, totalFaltas, totalTardanzas, porCurso, meses);
    }

    private Matricula elegirMatricula(List<Matricula> matriculas, Long cicloId) {
        if (matriculas == null || matriculas.isEmpty()) {
            return null;
        }
        if (cicloId != null) {
            return matriculas.stream().filter(m -> cicloId.equals(m.getSemestre().getId())).findFirst().orElse(null);
        }
        return matriculas.stream()
                .max(Comparator.comparing(Matricula::getFechaMatricula))
                .orElse(null);
    }

    private static EstadoAsistenciaDia peorEstado(EstadoAsistenciaDia a, EstadoAsistenciaDia b) {
        if (a == EstadoAsistenciaDia.FALTA || b == EstadoAsistenciaDia.FALTA) return EstadoAsistenciaDia.FALTA;
        if (a == EstadoAsistenciaDia.TARDANZA || b == EstadoAsistenciaDia.TARDANZA) return EstadoAsistenciaDia.TARDANZA;
        return EstadoAsistenciaDia.PRESENTE;
    }

    private LocalDate maxFecha(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate minFecha(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private List<MesCalendarioAsistenciaDTO> construirCalendario(LocalDate desde, LocalDate hasta,
                                                                  Map<LocalDate, EstadoAsistenciaDia> estadoPorDia) {
        List<MesCalendarioAsistenciaDTO> meses = new ArrayList<>();
        YearMonth mesActual = YearMonth.from(desde);
        YearMonth mesFinal = YearMonth.from(hasta);

        while (!mesActual.isAfter(mesFinal)) {
            LocalDate primerDiaMes = mesActual.atDay(1);
            LocalDate ultimoDiaMes = mesActual.atEndOfMonth();

            LocalDate cursor = primerDiaMes.minusDays(primerDiaMes.getDayOfWeek().getValue() - 1);
            LocalDate finGrilla = ultimoDiaMes.plusDays(7 - ultimoDiaMes.getDayOfWeek().getValue());

            List<List<DiaCalendarioAsistenciaDTO>> semanas = new ArrayList<>();
            List<DiaCalendarioAsistenciaDTO> semana = new ArrayList<>();
            for (LocalDate d = cursor; !d.isAfter(finGrilla); d = d.plusDays(1)) {
                boolean dentroDelMes = d.getMonth() == mesActual.getMonth() && d.getYear() == mesActual.getYear();
                boolean dentroDelRango = !d.isBefore(desde) && !d.isAfter(hasta);
                EstadoAsistenciaDia estado = null;
                if (dentroDelMes && dentroDelRango) {
                    estado = estadoPorDia.getOrDefault(d, EstadoAsistenciaDia.SIN_CLASE);
                }
                semana.add(new DiaCalendarioAsistenciaDTO(dentroDelMes ? d : null, estado));
                if (semana.size() == 7) {
                    semanas.add(semana);
                    semana = new ArrayList<>();
                }
            }

            String etiqueta = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "PE"))
                    + " " + mesActual.getYear();
            etiqueta = Character.toUpperCase(etiqueta.charAt(0)) + etiqueta.substring(1);
            meses.add(new MesCalendarioAsistenciaDTO(mesActual, etiqueta, semanas));
            mesActual = mesActual.plusMonths(1);
        }
        return meses;
    }
}
