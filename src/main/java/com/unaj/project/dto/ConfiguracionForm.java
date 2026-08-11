package com.unaj.project.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ConfiguracionForm {

    @NotNull(message = "El precio de matrícula es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de matrícula debe ser mayor a 0")
    private BigDecimal montoMatricula;

    @NotNull(message = "El precio de pensión es obligatorio")
    @DecimalMin(value = "0", message = "El precio de pensión no puede ser negativo")
    private BigDecimal montoPension;

    @NotNull(message = "El número de cuotas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 cuota")
    private Integer numeroCuotasPension;

    @NotNull(message = "Los días entre cuotas son obligatorios")
    @Min(value = 1, message = "Debe ser al menos 1 día")
    private Integer diasEntreCuotas;

    @NotNull(message = "Los días de gracia son obligatorios")
    @Min(value = 0, message = "No puede ser negativo")
    private Integer diasGraciaVencimiento;

    @NotNull(message = "El cupo por turno es obligatorio")
    @Min(value = 1, message = "Debe ser al menos 1")
    private Integer cupoPorTurno;

    @NotNull(message = "La tolerancia es obligatoria")
    @Min(value = 0, message = "No puede ser negativa")
    private Integer toleranciaMinutosHorasDocentes;

    @NotNull(message = "Los días de aviso son obligatorios")
    @Min(value = 1, message = "Debe ser al menos 1 día")
    private Integer diasAvisoVencimiento;

    @NotBlank(message = "El nombre de la academia es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombreAcademia;

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{6,20}$", message = "El teléfono no tiene un formato válido")
    private String telefonoContacto;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String direccion;

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "El color debe tener el formato #RRGGBB")
    private String colorAcento;

    @NotBlank(message = "El símbolo de moneda es obligatorio")
    @Size(max = 5, message = "Máximo 5 caracteres")
    private String simboloMoneda;

    @Size(max = 120, message = "Máximo 120 caracteres")
    private String eslogan;

    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String correoContacto;

    private MultipartFile logo;

    private boolean quitarLogo;

    private MultipartFile favicon;

    private boolean quitarFavicon;

    private MultipartFile fondoLogin;

    private boolean quitarFondoLogin;

    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String vision;

    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String mision;

    @Size(max = 4000, message = "Máximo 4000 caracteres")
    private String sobreNosotros;

    private boolean mostrarVisionMision = true;

    private boolean mostrarDocentes = true;

    private boolean mostrarCursos = true;

    private boolean mostrarSedes = true;

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{6,20}$", message = "El teléfono no tiene un formato válido")
    private String whatsappNumero;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String facebookUrl;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String instagramUrl;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String tiktokUrl;

    @Min(value = 1900, message = "Año inválido")
    private Integer anioFundacion;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String descripcionIngenierias;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String descripcionBiomedicas;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String descripcionSociales;

    private boolean mostrarProcesoAdmision = true;

    private boolean mostrarFaq = true;

    private boolean mostrarTestimonios = true;

    private boolean mostrarLogros = true;

    private boolean mostrarGaleria = true;

    private boolean mostrarCalendario = true;

    private boolean mostrarFormularioContacto = true;

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

    public MultipartFile getLogo() { return logo; }
    public void setLogo(MultipartFile logo) { this.logo = logo; }

    public boolean isQuitarLogo() { return quitarLogo; }
    public void setQuitarLogo(boolean quitarLogo) { this.quitarLogo = quitarLogo; }

    public String getSimboloMoneda() { return simboloMoneda; }
    public void setSimboloMoneda(String simboloMoneda) { this.simboloMoneda = simboloMoneda; }

    public String getEslogan() { return eslogan; }
    public void setEslogan(String eslogan) { this.eslogan = eslogan; }

    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }

    public MultipartFile getFavicon() { return favicon; }
    public void setFavicon(MultipartFile favicon) { this.favicon = favicon; }

    public boolean isQuitarFavicon() { return quitarFavicon; }
    public void setQuitarFavicon(boolean quitarFavicon) { this.quitarFavicon = quitarFavicon; }

    public MultipartFile getFondoLogin() { return fondoLogin; }
    public void setFondoLogin(MultipartFile fondoLogin) { this.fondoLogin = fondoLogin; }

    public boolean isQuitarFondoLogin() { return quitarFondoLogin; }
    public void setQuitarFondoLogin(boolean quitarFondoLogin) { this.quitarFondoLogin = quitarFondoLogin; }

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
}
