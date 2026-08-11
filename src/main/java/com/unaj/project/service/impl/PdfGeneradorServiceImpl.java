package com.unaj.project.service.impl;

import com.unaj.project.model.Configuracion;
import com.unaj.project.service.ConfiguracionService;
import com.unaj.project.service.PdfGeneradorService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;

@Service
public class PdfGeneradorServiceImpl implements PdfGeneradorService {

    private final TemplateEngine templateEngine;
    private final ConfiguracionService configuracionService;

    public PdfGeneradorServiceImpl(TemplateEngine templateEngine, ConfiguracionService configuracionService) {
        this.templateEngine = templateEngine;
        this.configuracionService = configuracionService;
    }

    @Override
    public byte[] renderizar(String template, Context context) {
        context.setVariable("fechaGeneracion", LocalDate.now());
        Configuracion configuracion = configuracionService.obtener();
        context.setVariable("marcaNombre", configuracion.getNombreAcademia());
        context.setVariable("marcaMoneda", configuracion.getSimboloMoneda());
        String logoDataUri = null;
        if (configuracion.isLogoPresente()) {
            String tipo = (configuracion.getLogoContentType() != null) ? configuracion.getLogoContentType() : "image/png";
            logoDataUri = "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(configuracion.getLogo());
        }
        context.setVariable("marcaLogoDataUri", logoDataUri);
        String html = templateEngine.process(template, context);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF.", e);
        }
    }
}
