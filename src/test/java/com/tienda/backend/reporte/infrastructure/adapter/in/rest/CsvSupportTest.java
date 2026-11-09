package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSupportTest {

    record Fila(Long id, String nombre, BigDecimal importe) {
    }

    private String csv(List<Fila> filas) {
        ResponseEntity<byte[]> r = CsvSupport.respuesta("prueba", filas, Fila.class);
        byte[] cuerpo = r.getBody();
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, java.util.Arrays.copyOf(cuerpo, 3),
                "debe llevar BOM para que Excel lea las tildes");
        return new String(cuerpo, 3, cuerpo.length - 3, StandardCharsets.UTF_8);
    }

    @Test
    void escribeEncabezadoYFilas() {
        assertEquals("id,nombre,importe\r\n1,Arroz,12.50\r\n",
                csv(List.of(new Fila(1L, "Arroz", new BigDecimal("12.50")))));
    }

    @Test
    void entrecomillaComasYComillasYDejaVaciosLosNulos() {
        assertEquals("id,nombre,importe\r\n,\"Sal, fina\",\r\n2,\"Dijo \"\"hola\"\"\",5\r\n",
                csv(List.of(new Fila(null, "Sal, fina", null), new Fila(2L, "Dijo \"hola\"", new BigDecimal("5")))));
    }

    @Test
    void neutralizaTextosQueExcelInterpretariaComoFormula() {
        assertEquals("id,nombre,importe\r\n1,'=SUMA(A1),\r\n",
                csv(List.of(new Fila(1L, "=SUMA(A1)", null))).replace("\"'=SUMA(A1)\"", "'=SUMA(A1)"));
    }

    @Test
    void usaPuntoDecimalSinNotacionCientifica() {
        assertEquals("id,nombre,importe\r\n1,x,0.00001\r\n",
                csv(List.of(new Fila(1L, "x", new BigDecimal("1E-5")))));
    }
}
