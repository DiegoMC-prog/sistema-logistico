package com.tienda.backend.shared.infrastructure.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Crea documentos PDF con el membrete de la tienda. */
@Component
public class PlantillaPdf {

    private final String tienda;

    public PlantillaPdf(@Value("${app.mail.tienda-nombre:Tienda}") String tienda) {
        this.tienda = tienda;
    }

    /** Documento tamaño carta con el nombre de la tienda a la izquierda y el título y número a la derecha. */
    public DocumentoPdf nuevo(String titulo, String numero) {
        return new DocumentoPdf(tienda, titulo, numero);
    }
}
