package com.unaj.project.service;

public interface QrCodeService {
    /** Genera un código QR como PNG codificando el contenido dado, de tamaño (tamano x tamano) px. */
    byte[] generarPng(String contenido, int tamano);
}
