package com.unaj.project.service.impl;

import com.unaj.project.dto.FilaHorarioDTO;
import com.unaj.project.exception.RecursoNoEncontradoException;
import com.unaj.project.model.*;
import com.unaj.project.repository.BloqueHorarioRepository;
import com.unaj.project.repository.CicloRepository;
import com.unaj.project.repository.CursoRepository;
import com.unaj.project.repository.HorarioRepository;
import com.unaj.project.repository.SalonRepository;
import com.unaj.project.service.HorarioService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final CicloRepository cicloRepository;
    private final CursoRepository cursoRepository;
    private final SalonRepository salonRepository;
    private final RegistroActividadService registroActividadService;

    public HorarioServiceImpl(HorarioRepository horarioRepository, BloqueHorarioRepository bloqueHorarioRepository,
                              CicloRepository cicloRepository, CursoRepository cursoRepository,
                              SalonRepository salonRepository,
                              RegistroActividadService registroActividadService) {
        this.horarioRepository = horarioRepository;
        this.bloqueHorarioRepository = bloqueHorarioRepository;
        this.cicloRepository = cicloRepository;
        this.cursoRepository = cursoRepository;
        this.salonRepository = salonRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    public Horario buscarPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado (id " + id + ")."));
    }

    @Override
    public BloqueHorario buscarBloque(Long id) {
        return bloqueHorarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloque horario no encontrado (id " + id + ")."));
    }

    @Override
    @Transactional
    public void crearBloque(Long cicloId, Nivel nivel, Turno turno, LocalTime horaInicio, LocalTime horaFin, TipoBloque tipo, String area, Long salonId) {
        if (horaInicio == null || horaFin == null || !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }
        if (nivel == null) {
            throw new IllegalArgumentException("Debe seleccionar un nivel.");
        }
        if (area == null || area.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un área.");
        }
        Long salonIdEfectivo = (nivel == Nivel.PREUNIVERSITARIO) ? salonId : null;
        if (bloqueHorarioRepository.existsByCicloIdAndNivelAndTurnoAndHoraInicioAndAreaAndSalon(
                cicloId, nivel, turno, horaInicio, area, salonIdEfectivo)) {
            throw new IllegalArgumentException("Ya existe un bloque que inicia a esa hora en este turno para esta área"
                    + (salonIdEfectivo != null ? " y salón" : "") + ".");
        }
        Ciclo ciclo = cicloRepository.findById(cicloId)
                .orElseThrow(() -> new IllegalArgumentException("Ciclo no encontrado: " + cicloId));
        Salon salon = null;
        if (salonIdEfectivo != null) {
            salon = salonRepository.findById(salonIdEfectivo)
                    .orElseThrow(() -> new IllegalArgumentException("Salón no encontrado: " + salonIdEfectivo));
        }

        BloqueHorario bloque = new BloqueHorario();
        bloque.setCiclo(ciclo);
        bloque.setNivel(nivel);
        bloque.setTurno(turno);
        bloque.setHoraInicio(horaInicio);
        bloque.setHoraFin(horaFin);
        bloque.setTipo(tipo != null ? tipo : TipoBloque.CLASE);
        bloque.setArea(area);
        bloque.setSalon(salon);
        BloqueHorario guardado = bloqueHorarioRepository.save(bloque);
        registroActividadService.registrar(TipoAccion.CREAR, "Horarios", guardado.getId(),
                "Creó un bloque horario de " + ciclo.getNombre() + " (" + nivel.getEtiqueta() + ", " + area
                        + (salon != null ? ", " + salon.getNombre() : "") + ", " + horaInicio + "-" + horaFin + ")");
    }

    @Override
    @Transactional
    public void eliminarBloque(Long bloqueId) {
        BloqueHorario bloque = buscarBloque(bloqueId);
        String descripcion = "Eliminó un bloque horario de " + bloque.getCiclo().getNombre()
                + " (" + bloque.getNivel().getEtiqueta() + ", " + bloque.getArea() + ")";
        bloqueHorarioRepository.deleteById(bloqueId);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Horarios", bloqueId, descripcion);
    }

    @Override
    public Map<Turno, List<FilaHorarioDTO>> agruparParaGrilla(Long cicloId, Nivel nivel, String area, Long salonId) {
        Map<Turno, List<FilaHorarioDTO>> resultado = new LinkedHashMap<>();
        for (Turno t : Turno.values()) {
            resultado.put(t, new ArrayList<>());
        }
        if (cicloId == null || nivel == null || area == null || area.isBlank()) return resultado;

        Long salonIdEfectivo = (nivel == Nivel.PREUNIVERSITARIO) ? salonId : null;
        List<BloqueHorario> bloques =
                bloqueHorarioRepository.findByCicloIdAndNivelAndAreaAndSalonOrderByHoraInicioAsc(cicloId, nivel, area, salonIdEfectivo);
        List<Horario> horarios = horarioRepository.findByCicloId(cicloId);

        Map<Long, Map<DiaSemana, List<Horario>>> porBloque = new LinkedHashMap<>();
        for (Horario h : horarios) {
            porBloque.computeIfAbsent(h.getBloque().getId(), k -> new EnumMap<>(DiaSemana.class))
                    .computeIfAbsent(h.getDiaSemana(), k -> new ArrayList<>())
                    .add(h);
        }

        for (BloqueHorario bloque : bloques) {
            Map<DiaSemana, List<Horario>> porDia = porBloque.getOrDefault(bloque.getId(), Map.of());
            resultado.get(bloque.getTurno()).add(new FilaHorarioDTO(bloque, porDia));
        }
        return resultado;
    }

    @Override
    public Map<Nivel, Long> contarBloquesPorNivel(Long cicloId) {
        Map<Nivel, Long> conteo = new LinkedHashMap<>();
        for (Nivel nivel : Nivel.values()) {
            conteo.put(nivel, 0L);
        }
        if (cicloId == null) return conteo;
        for (BloqueHorario bloque : bloqueHorarioRepository.findByCicloId(cicloId)) {
            conteo.merge(bloque.getNivel(), 1L, Long::sum);
        }
        return conteo;
    }

    @Override
    public Map<String, Long> contarBloquesPorArea(Long cicloId, Nivel nivel) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String area : Areas.paraNivel(nivel)) {
            conteo.put(area, 0L);
        }
        if (cicloId == null) return conteo;
        for (BloqueHorario bloque : bloqueHorarioRepository.findByCicloId(cicloId)) {
            if (bloque.getNivel() == nivel && conteo.containsKey(bloque.getArea())) {
                conteo.merge(bloque.getArea(), 1L, Long::sum);
            }
        }
        return conteo;
    }

    @Override
    @Transactional
    public void asignarCurso(Long bloqueId, DiaSemana dia, List<Long> cursoIds) {
        BloqueHorario bloque = buscarBloque(bloqueId);
        if (bloque.isReceso()) {
            throw new IllegalArgumentException("No se pueden asignar cursos a un bloque de receso.");
        }
        if (cursoIds == null || cursoIds.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un curso.");
        }

        for (Long cursoId : cursoIds) {
            if (horarioRepository.existsByBloqueIdAndDiaSemanaAndCursoId(bloqueId, dia, cursoId)) {
                continue;
            }
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));
            if (curso.getNivel() != bloque.getNivel()) {
                throw new IllegalArgumentException(
                        "El curso \"" + curso.getNombre() + "\" no pertenece al nivel " + bloque.getNivel().getEtiqueta() + ".");
            }
            if (!curso.getAreas().contains(bloque.getArea())) {
                throw new IllegalArgumentException(
                        "El curso \"" + curso.getNombre() + "\" no pertenece al área " + bloque.getArea() + ".");
            }
            Horario horario = new Horario();
            horario.setBloque(bloque);
            horario.setDiaSemana(dia);
            horario.setCurso(curso);
            horarioRepository.save(horario);
            registroActividadService.registrar(TipoAccion.CREAR, "Horarios", horario.getId(),
                    "Asignó el curso " + curso.getNombre() + " al horario de " + dia.getEtiqueta());
        }
    }

    @Override
    @Transactional
    public void quitarCurso(Long horarioId) {
        Horario horario = buscarPorId(horarioId);
        String descripcion = "Quitó el curso " + horario.getCurso().getNombre()
                + " del horario de " + horario.getDiaSemana().getEtiqueta();
        horarioRepository.deleteById(horarioId);
        registroActividadService.registrar(TipoAccion.ELIMINAR, "Horarios", horarioId, descripcion);
    }

    @Override
    public List<Horario> listarPorCicloTurnoDia(Long cicloId, Turno turno, DiaSemana dia) {
        if (cicloId == null || turno == null || dia == null) {
            return List.of();
        }
        return horarioRepository.findByCicloIdAndTurnoAndDiaSemana(cicloId, turno, dia);
    }
}
