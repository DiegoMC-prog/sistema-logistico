package com.tienda.backend.shared.infrastructure.pdf;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.text.Normalizer;

public final class RespuestaPdf {

    private RespuestaPdf() {
    }

    /** Respuesta de descarga; el nombre se limita a caracteres seguros para un archivo. */
    public static ResponseEntity<byte[]> descarga(byte[] contenido, String nombre) {
        // Primero se quitan los acentos (Sofía -> Sofia) para no perder letras
        String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String archivo = sinAcentos.replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo).build().toString())
                .body(contenido);
    }
}
