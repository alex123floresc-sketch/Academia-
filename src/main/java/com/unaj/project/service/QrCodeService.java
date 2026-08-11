package com.unaj.project.service;

public interface QrCodeService {
    byte[] generarPng(String contenido, int tamano);
}
