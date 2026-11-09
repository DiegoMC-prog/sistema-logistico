package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exporta una lista de registros ({@code record}) a CSV: una columna por componente, con el nombre del componente
 * como encabezado. Usa coma como separador y UTF-8 con marca de orden de bytes para que Excel lea bien las tildes.
 */
final class CsvSupport {
    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvSupport() {
    }

    static ResponseEntity<byte[]> respuesta(String nombreArchivo, List<? extends Record> filas, Class<? extends Record> tipo) {
        RecordComponent[] columnas = tipo.getRecordComponents();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnas.length; i++) {
            sb.append(i == 0 ? "" : ",").append(escapar(columnas[i].getName()));
        }
        sb.append("\r\n");
        for (Record fila : filas) {
            for (int i = 0; i < columnas.length; i++) {
                sb.append(i == 0 ? "" : ",").append(escapar(valor(fila, columnas[i])));
            }
            sb.append("\r\n");
        }

        byte[] texto = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] cuerpo = new byte[BOM.length + texto.length];
        System.arraycopy(BOM, 0, cuerpo, 0, BOM.length);
        System.arraycopy(texto, 0, cuerpo, BOM.length, texto.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo + ".csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(cuerpo);
    }

    private static String valor(Record fila, RecordComponent componente) {
        try {
            Object v = componente.getAccessor().invoke(fila);
            if (v == null) {
                return "";
            }
            return v instanceof BigDecimal b ? b.toPlainString() : v.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("No se pudo leer la columna " + componente.getName(), e);
        }
    }

    /** Comillas si el texto lleva coma, comillas o saltos de línea. Y neutraliza fórmulas de hoja de cálculo. */
    private static String escapar(String texto) {
        String t = texto;
        // Un texto que empieza con = + - @ se interpretaría como fórmula al abrirlo en Excel
        if (!t.isEmpty() && "=+@".indexOf(t.charAt(0)) >= 0) {
            t = "'" + t;
        }
        if (t.contains(",") || t.contains("\"") || t.contains("\n") || t.contains("\r")) {
            return "\"" + t.replace("\"", "\"\"") + "\"";
        }
        return t;
    }
}
