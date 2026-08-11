package com.unaj.project.service.impl;

import com.unaj.project.dto.ConfiguracionForm;
import com.unaj.project.model.Configuracion;
import com.unaj.project.model.TipoAccion;
import com.unaj.project.repository.ConfiguracionRepository;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.RegistroActividadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private static final Long ID = 1L;
    private static final BigDecimal MONTO_MATRICULA_INICIAL = new BigDecimal("150.00");
    private static final BigDecimal MONTO_PENSION_INICIAL = new BigDecimal("180.00");
    private static final int NUMERO_CUOTAS_INICIAL = 1;
    private static final int DIAS_ENTRE_CUOTAS_INICIAL = 30;
    private static final int DIAS_GRACIA_INICIAL = 0;
    private static final int CUPO_POR_TURNO_INICIAL = 60;
    private static final int TOLERANCIA_MINUTOS_INICIAL = 30;
    private static final int DIAS_AVISO_VENCIMIENTO_INICIAL = 15;
    private static final String NOMBRE_ACADEMIA_INICIAL = "Lapreplus";
    private static final String SIMBOLO_MONEDA_INICIAL = "S/";

    private final ConfiguracionRepository configuracionRepository;
    private final RegistroActividadService registroActividadService;

    public ConfiguracionServiceImpl(ConfiguracionRepository configuracionRepository,
                                    RegistroActividadService registroActividadService) {
        this.configuracionRepository = configuracionRepository;
        this.registroActividadService = registroActividadService;
    }

    @Override
    @Transactional
    public Configuracion obtener() {
        Configuracion configuracion = configuracionRepository.findById(ID).orElseGet(() -> {
            Configuracion nueva = new Configuracion();
            nueva.setMontoMatricula(MONTO_MATRICULA_INICIAL);
            nueva.setMontoPension(MONTO_PENSION_INICIAL);
            return nueva;
        });

        boolean incompleta = false;
        if (configuracion.getNumeroCuotasPension() == null) {
            configuracion.setNumeroCuotasPension(NUMERO_CUOTAS_INICIAL);
            incompleta = true;
        }
        if (configuracion.getDiasEntreCuotas() == null) {
            configuracion.setDiasEntreCuotas(DIAS_ENTRE_CUOTAS_INICIAL);
            incompleta = true;
        }
        if (configuracion.getDiasGraciaVencimiento() == null) {
            configuracion.setDiasGraciaVencimiento(DIAS_GRACIA_INICIAL);
            incompleta = true;
        }
        if (configuracion.getCupoPorTurno() == null) {
            configuracion.setCupoPorTurno(CUPO_POR_TURNO_INICIAL);
            incompleta = true;
        }
        if (configuracion.getToleranciaMinutosHorasDocentes() == null) {
            configuracion.setToleranciaMinutosHorasDocentes(TOLERANCIA_MINUTOS_INICIAL);
            incompleta = true;
        }
        if (configuracion.getDiasAvisoVencimiento() == null) {
            configuracion.setDiasAvisoVencimiento(DIAS_AVISO_VENCIMIENTO_INICIAL);
            incompleta = true;
        }
        if (configuracion.getNombreAcademia() == null || configuracion.getNombreAcademia().isBlank()) {
            configuracion.setNombreAcademia(NOMBRE_ACADEMIA_INICIAL);
            incompleta = true;
        }
        if (configuracion.getSimboloMoneda() == null || configuracion.getSimboloMoneda().isBlank()) {
            configuracion.setSimboloMoneda(SIMBOLO_MONEDA_INICIAL);
            incompleta = true;
        }
        if (configuracion.getId() == null || incompleta) {
            configuracion = configuracionRepository.save(configuracion);
        }
        return configuracion;
    }

    @Override
    @Transactional
    public void actualizar(ConfiguracionForm form) {
        Configuracion configuracion = obtener();
        configuracion.setMontoMatricula(form.getMontoMatricula());
        configuracion.setMontoPension(form.getMontoPension());
        configuracion.setNumeroCuotasPension(form.getNumeroCuotasPension());
        configuracion.setDiasEntreCuotas(form.getDiasEntreCuotas());
        configuracion.setDiasGraciaVencimiento(form.getDiasGraciaVencimiento());
        configuracion.setCupoPorTurno(form.getCupoPorTurno());
        configuracion.setToleranciaMinutosHorasDocentes(form.getToleranciaMinutosHorasDocentes());
        configuracion.setDiasAvisoVencimiento(form.getDiasAvisoVencimiento());
        configuracion.setNombreAcademia(form.getNombreAcademia());
        configuracion.setTelefonoContacto(blankToNull(form.getTelefonoContacto()));
        configuracion.setDireccion(blankToNull(form.getDireccion()));
        configuracion.setColorAcento(blankToNull(form.getColorAcento()));
        configuracion.setSimboloMoneda(form.getSimboloMoneda());
        configuracion.setEslogan(blankToNull(form.getEslogan()));
        configuracion.setCorreoContacto(blankToNull(form.getCorreoContacto()));
        configuracion.setVision(blankToNull(form.getVision()));
        configuracion.setMision(blankToNull(form.getMision()));
        configuracion.setSobreNosotros(blankToNull(form.getSobreNosotros()));
        configuracion.setMostrarVisionMision(form.isMostrarVisionMision());
        configuracion.setMostrarDocentes(form.isMostrarDocentes());
        configuracion.setMostrarCursos(form.isMostrarCursos());
        configuracion.setMostrarSedes(form.isMostrarSedes());
        configuracion.setWhatsappNumero(blankToNull(form.getWhatsappNumero()));
        configuracion.setFacebookUrl(blankToNull(form.getFacebookUrl()));
        configuracion.setInstagramUrl(blankToNull(form.getInstagramUrl()));
        configuracion.setTiktokUrl(blankToNull(form.getTiktokUrl()));
        configuracion.setAnioFundacion(form.getAnioFundacion());
        configuracion.setDescripcionIngenierias(blankToNull(form.getDescripcionIngenierias()));
        configuracion.setDescripcionBiomedicas(blankToNull(form.getDescripcionBiomedicas()));
        configuracion.setDescripcionSociales(blankToNull(form.getDescripcionSociales()));
        configuracion.setMostrarProcesoAdmision(form.isMostrarProcesoAdmision());
        configuracion.setMostrarFaq(form.isMostrarFaq());
        configuracion.setMostrarTestimonios(form.isMostrarTestimonios());
        configuracion.setMostrarLogros(form.isMostrarLogros());
        configuracion.setMostrarGaleria(form.isMostrarGaleria());
        configuracion.setMostrarCalendario(form.isMostrarCalendario());
        configuracion.setMostrarFormularioContacto(form.isMostrarFormularioContacto());

        if (form.isQuitarLogo()) {
            configuracion.setLogo(null);
            configuracion.setLogoContentType(null);
        }
        MultipartFile logo = form.getLogo();
        if (logo != null && !logo.isEmpty()) {
            try {
                configuracion.setLogo(logo.getBytes());
                configuracion.setLogoContentType(logo.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer el logo enviado.", e);
            }
        }

        if (form.isQuitarFavicon()) {
            configuracion.setFavicon(null);
            configuracion.setFaviconContentType(null);
        }
        MultipartFile favicon = form.getFavicon();
        if (favicon != null && !favicon.isEmpty()) {
            try {
                configuracion.setFavicon(favicon.getBytes());
                configuracion.setFaviconContentType(favicon.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer el favicon enviado.", e);
            }
        }

        if (form.isQuitarFondoLogin()) {
            configuracion.setFondoLogin(null);
            configuracion.setFondoLoginContentType(null);
        }
        MultipartFile fondoLogin = form.getFondoLogin();
        if (fondoLogin != null && !fondoLogin.isEmpty()) {
            try {
                configuracion.setFondoLogin(fondoLogin.getBytes());
                configuracion.setFondoLoginContentType(fondoLogin.getContentType());
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer la imagen de fondo enviada.", e);
            }
        }

        configuracionRepository.save(configuracion);
        registroActividadService.registrar(TipoAccion.EDITAR, "Configuración", configuracion.getId(),
                "Actualizó la configuración del sistema");
    }

    private String blankToNull(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
