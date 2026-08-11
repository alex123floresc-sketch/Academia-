package com.unaj.project.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    private Long id = 1L;

    @Column(name = "monto_matricula", nullable = false)
    private BigDecimal montoMatricula;

    @Column(name = "monto_pension", nullable = false)
    private BigDecimal montoPension;

    @Column(name = "numero_cuotas_pension")
    private Integer numeroCuotasPension;

    @Column(name = "dias_entre_cuotas")
    private Integer diasEntreCuotas;

    @Column(name = "dias_gracia_vencimiento")
    private Integer diasGraciaVencimiento;

    @Column(name = "cupo_por_turno")
    private Integer cupoPorTurno;

    @Column(name = "tolerancia_minutos_horas_docentes")
    private Integer toleranciaMinutosHorasDocentes;

    @Column(name = "dias_aviso_vencimiento")
    private Integer diasAvisoVencimiento;

    @Column(name = "nombre_academia", length = 80)
    private String nombreAcademia;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(name = "direccion", length = 150)
    private String direccion;

    @Column(name = "color_acento", length = 7)
    private String colorAcento;

    @Column(name = "simbolo_moneda", length = 5)
    private String simboloMoneda;

    @Column(name = "eslogan", length = 120)
    private String eslogan;

    @Column(name = "correo_contacto", length = 100)
    private String correoContacto;

    @Lob
    @Column(name = "vision", columnDefinition = "TEXT")
    private String vision;

    @Lob
    @Column(name = "mision", columnDefinition = "TEXT")
    private String mision;

    @Lob
    @Column(name = "sobre_nosotros", columnDefinition = "TEXT")
    private String sobreNosotros;

    @Column(name = "mostrar_vision_mision", nullable = false)
    private boolean mostrarVisionMision = true;

    @Column(name = "mostrar_docentes", nullable = false)
    private boolean mostrarDocentes = true;

    @Column(name = "mostrar_cursos", nullable = false)
    private boolean mostrarCursos = true;

    @Column(name = "mostrar_sedes", nullable = false)
    private boolean mostrarSedes = true;

    @Column(name = "whatsapp_numero", length = 20)
    private String whatsappNumero;

    @Column(name = "facebook_url", length = 200)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 200)
    private String instagramUrl;

    @Column(name = "tiktok_url", length = 200)
    private String tiktokUrl;

    @Column(name = "anio_fundacion")
    private Integer anioFundacion;

    @Column(name = "descripcion_ingenierias", length = 300)
    private String descripcionIngenierias;

    @Column(name = "descripcion_biomedicas", length = 300)
    private String descripcionBiomedicas;

    @Column(name = "descripcion_sociales", length = 300)
    private String descripcionSociales;

    @Column(name = "mostrar_proceso_admision", nullable = false)
    private boolean mostrarProcesoAdmision = true;

    @Column(name = "mostrar_faq", nullable = false)
    private boolean mostrarFaq = true;

    @Column(name = "mostrar_testimonios", nullable = false)
    private boolean mostrarTestimonios = true;

    @Column(name = "mostrar_logros", nullable = false)
    private boolean mostrarLogros = true;

    @Column(name = "mostrar_galeria", nullable = false)
    private boolean mostrarGaleria = true;

    @Column(name = "mostrar_calendario", nullable = false)
    private boolean mostrarCalendario = true;

    @Column(name = "mostrar_formulario_contacto", nullable = false)
    private boolean mostrarFormularioContacto = true;

    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;

    @Column(name = "logo_content_type")
    private String logoContentType;

    @Lob
    @Column(name = "favicon", columnDefinition = "LONGBLOB")
    private byte[] favicon;

    @Column(name = "favicon_content_type")
    private String faviconContentType;

    @Lob
    @Column(name = "fondo_login", columnDefinition = "LONGBLOB")
    private byte[] fondoLogin;

    @Column(name = "fondo_login_content_type")
    private String fondoLoginContentType;

    public Configuracion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontoMatricula() { return montoMatricula; }
    public void setMontoMatricula(BigDecimal montoMatricula) { this.montoMatricula = montoMatricula; }

    public BigDecimal getMontoPension() { return montoPension; }
    public void setMontoPension(BigDecimal montoPension) { this.montoPension = montoPension; }

    public Integer getNumeroCuotasPension() { return numeroCuotasPension; }
    public void setNumeroCuotasPension(Integer numeroCuotasPension) { this.numeroCuotasPension = numeroCuotasPension; }

    public Integer getDiasEntreCuotas() { return diasEntreCuotas; }
    public void setDiasEntreCuotas(Integer diasEntreCuotas) { this.diasEntreCuotas = diasEntreCuotas; }

    public Integer getDiasGraciaVencimiento() { return diasGraciaVencimiento; }
    public void setDiasGraciaVencimiento(Integer diasGraciaVencimiento) { this.diasGraciaVencimiento = diasGraciaVencimiento; }

    public Integer getCupoPorTurno() { return cupoPorTurno; }
    public void setCupoPorTurno(Integer cupoPorTurno) { this.cupoPorTurno = cupoPorTurno; }

    public Integer getToleranciaMinutosHorasDocentes() { return toleranciaMinutosHorasDocentes; }
    public void setToleranciaMinutosHorasDocentes(Integer toleranciaMinutosHorasDocentes) { this.toleranciaMinutosHorasDocentes = toleranciaMinutosHorasDocentes; }

    public Integer getDiasAvisoVencimiento() { return diasAvisoVencimiento; }
    public void setDiasAvisoVencimiento(Integer diasAvisoVencimiento) { this.diasAvisoVencimiento = diasAvisoVencimiento; }

    public String getNombreAcademia() { return nombreAcademia; }
    public void setNombreAcademia(String nombreAcademia) { this.nombreAcademia = nombreAcademia; }

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getColorAcento() { return colorAcento; }
    public void setColorAcento(String colorAcento) { this.colorAcento = colorAcento; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public String getLogoContentType() { return logoContentType; }
    public void setLogoContentType(String logoContentType) { this.logoContentType = logoContentType; }

    public boolean isLogoPresente() { return logo != null && logo.length > 0; }

    public String getSimboloMoneda() { return simboloMoneda; }
    public void setSimboloMoneda(String simboloMoneda) { this.simboloMoneda = simboloMoneda; }

    public String getEslogan() { return eslogan; }
    public void setEslogan(String eslogan) { this.eslogan = eslogan; }

    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }

    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }

    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }

    public String getSobreNosotros() { return sobreNosotros; }
    public void setSobreNosotros(String sobreNosotros) { this.sobreNosotros = sobreNosotros; }

    public boolean isMostrarVisionMision() { return mostrarVisionMision; }
    public void setMostrarVisionMision(boolean mostrarVisionMision) { this.mostrarVisionMision = mostrarVisionMision; }

    public boolean isMostrarDocentes() { return mostrarDocentes; }
    public void setMostrarDocentes(boolean mostrarDocentes) { this.mostrarDocentes = mostrarDocentes; }

    public boolean isMostrarCursos() { return mostrarCursos; }
    public void setMostrarCursos(boolean mostrarCursos) { this.mostrarCursos = mostrarCursos; }

    public boolean isMostrarSedes() { return mostrarSedes; }
    public void setMostrarSedes(boolean mostrarSedes) { this.mostrarSedes = mostrarSedes; }

    public String getWhatsappNumero() { return whatsappNumero; }
    public void setWhatsappNumero(String whatsappNumero) { this.whatsappNumero = whatsappNumero; }

    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }

    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }

    public String getTiktokUrl() { return tiktokUrl; }
    public void setTiktokUrl(String tiktokUrl) { this.tiktokUrl = tiktokUrl; }

    public Integer getAnioFundacion() { return anioFundacion; }
    public void setAnioFundacion(Integer anioFundacion) { this.anioFundacion = anioFundacion; }

    public String getDescripcionIngenierias() { return descripcionIngenierias; }
    public void setDescripcionIngenierias(String descripcionIngenierias) { this.descripcionIngenierias = descripcionIngenierias; }

    public String getDescripcionBiomedicas() { return descripcionBiomedicas; }
    public void setDescripcionBiomedicas(String descripcionBiomedicas) { this.descripcionBiomedicas = descripcionBiomedicas; }

    public String getDescripcionSociales() { return descripcionSociales; }
    public void setDescripcionSociales(String descripcionSociales) { this.descripcionSociales = descripcionSociales; }

    public boolean isMostrarProcesoAdmision() { return mostrarProcesoAdmision; }
    public void setMostrarProcesoAdmision(boolean mostrarProcesoAdmision) { this.mostrarProcesoAdmision = mostrarProcesoAdmision; }

    public boolean isMostrarFaq() { return mostrarFaq; }
    public void setMostrarFaq(boolean mostrarFaq) { this.mostrarFaq = mostrarFaq; }

    public boolean isMostrarTestimonios() { return mostrarTestimonios; }
    public void setMostrarTestimonios(boolean mostrarTestimonios) { this.mostrarTestimonios = mostrarTestimonios; }

    public boolean isMostrarLogros() { return mostrarLogros; }
    public void setMostrarLogros(boolean mostrarLogros) { this.mostrarLogros = mostrarLogros; }

    public boolean isMostrarGaleria() { return mostrarGaleria; }
    public void setMostrarGaleria(boolean mostrarGaleria) { this.mostrarGaleria = mostrarGaleria; }

    public boolean isMostrarCalendario() { return mostrarCalendario; }
    public void setMostrarCalendario(boolean mostrarCalendario) { this.mostrarCalendario = mostrarCalendario; }

    public boolean isMostrarFormularioContacto() { return mostrarFormularioContacto; }
    public void setMostrarFormularioContacto(boolean mostrarFormularioContacto) { this.mostrarFormularioContacto = mostrarFormularioContacto; }

    public byte[] getFavicon() { return favicon; }
    public void setFavicon(byte[] favicon) { this.favicon = favicon; }

    public String getFaviconContentType() { return faviconContentType; }
    public void setFaviconContentType(String faviconContentType) { this.faviconContentType = faviconContentType; }

    public boolean isFaviconPresente() { return favicon != null && favicon.length > 0; }

    public byte[] getFondoLogin() { return fondoLogin; }
    public void setFondoLogin(byte[] fondoLogin) { this.fondoLogin = fondoLogin; }

    public String getFondoLoginContentType() { return fondoLoginContentType; }
    public void setFondoLoginContentType(String fondoLoginContentType) { this.fondoLoginContentType = fondoLoginContentType; }

    public boolean isFondoLoginPresente() { return fondoLogin != null && fondoLogin.length > 0; }
}
