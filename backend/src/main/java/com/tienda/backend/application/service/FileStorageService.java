package com.tienda.backend.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String almacenar(MultipartFile archivo, String subdirectorio);
    void eliminar(String rutaRelativa);
}
