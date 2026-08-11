package com.unaj.project.service;

import org.thymeleaf.context.Context;

public interface PdfGeneradorService {
    byte[] renderizar(String template, Context context);
}
