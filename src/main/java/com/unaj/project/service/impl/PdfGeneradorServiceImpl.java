package com.unaj.project.service.impl;

import com.unaj.project.service.PdfGeneradorService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfGeneradorServiceImpl implements PdfGeneradorService {

    private final TemplateEngine templateEngine;

    public PdfGeneradorServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public byte[] renderizar(String template, Context context) {
        context.setVariable("fechaGeneracion", LocalDate.now());
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
