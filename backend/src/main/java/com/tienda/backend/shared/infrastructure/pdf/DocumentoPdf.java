package com.tienda.backend.shared.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Documento PDF tamaño carta (notas de venta, comprobantes y estados de cuenta). Se usa una sola vez. */
public class DocumentoPdf {

    private static final Color TINTA = new Color(15, 23, 42);
    private static final Color GRIS = new Color(100, 116, 139);
    private static final Color LINEA = new Color(203, 213, 225);
    private static final Color FONDO = new Color(241, 245, 249);
    private static final Color ROJO = new Color(185, 28, 28);

    private static final Font TITULO = fuente(FontFactory.HELVETICA_BOLD, 17, TINTA);
    private static final Font SUBTITULO = fuente(FontFactory.HELVETICA_BOLD, 11, TINTA);
    private static final Font NORMAL = fuente(FontFactory.HELVETICA, 10, TINTA);
    private static final Font NEGRITA = fuente(FontFactory.HELVETICA_BOLD, 10, TINTA);
    private static final Font TENUE = fuente(FontFactory.HELVETICA, 9, GRIS);
    private static final Font CABECERA = fuente(FontFactory.HELVETICA_BOLD, 9, GRIS);
    private static final Font ALERTA = fuente(FontFactory.HELVETICA_BOLD, 11, ROJO);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Document documento = new Document(PageSize.LETTER, 48, 48, 48, 48);

    DocumentoPdf(String tienda, String titulo, String numero) {
        PdfWriter.getInstance(documento, salida);
        documento.addTitle(titulo + (numero == null ? "" : " " + numero));
        documento.addAuthor(tienda);
        documento.open();

        PdfPTable membrete = tablaSinBordes(55, 45);
        membrete.addCell(celdaLibre(new Phrase(tienda, TITULO), Element.ALIGN_LEFT));
        Phrase derecha = new Phrase(18, titulo.toUpperCase(Locale.ROOT), SUBTITULO);
        if (numero != null) {
            derecha.add("\n" + numero);
        }
        membrete.addCell(celdaLibre(derecha, Element.ALIGN_RIGHT));
        membrete.setSpacingAfter(6);
        documento.add(membrete);

        PdfPTable regla = new PdfPTable(1);
        regla.setWidthPercentage(100);
        PdfPCell linea = new PdfPCell();
        linea.setBorder(Rectangle.TOP);
        linea.setBorderColor(TINTA);
        linea.setBorderWidthTop(1.2f);
        linea.setFixedHeight(10);
        regla.addCell(linea);
        documento.add(regla);
    }

    /** Dato de cabecera «Etiqueta: valor». */
    public DocumentoPdf dato(String etiqueta, String valor) {
        Paragraph p = new Paragraph(15);
        p.add(new Phrase(etiqueta + ": ", TENUE));
        p.add(new Phrase(valor, NORMAL));
        documento.add(p);
        return this;
    }

    /** Aviso destacado en rojo (p. ej. documento anulado). */
    public DocumentoPdf aviso(String texto) {
        Paragraph p = new Paragraph(texto, ALERTA);
        p.setSpacingBefore(8);
        documento.add(p);
        return this;
    }

    public DocumentoPdf seccion(String titulo) {
        Paragraph p = new Paragraph(titulo, SUBTITULO);
        p.setSpacingBefore(14);
        documento.add(p);
        return this;
    }

    /**
     * Tabla con cabecera que se repite en cada página.
     *
     * @param anchos        proporción de cada columna
     * @param primeraDerecha columna (desde 0) a partir de la cual el contenido se alinea a la derecha
     */
    public DocumentoPdf tabla(List<String> columnas, float[] anchos, List<List<String>> filas, int primeraDerecha) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setHeaderRows(1);
        for (int i = 0; i < columnas.size(); i++) {
            PdfPCell celda = celda(new Phrase(columnas.get(i), CABECERA), i >= primeraDerecha);
            celda.setBackgroundColor(FONDO);
            tabla.addCell(celda);
        }
        for (List<String> fila : filas) {
            for (int i = 0; i < fila.size(); i++) {
                tabla.addCell(celda(new Phrase(fila.get(i), NORMAL), i >= primeraDerecha));
            }
        }
        if (filas.isEmpty()) {
            PdfPCell vacia = celda(new Phrase("Sin registros.", TENUE), false);
            vacia.setColspan(columnas.size());
            tabla.addCell(vacia);
        }
        documento.add(tabla);
        return this;
    }

    /** Bloque de importes alineado a la derecha; la última fila marcada como destacada va en negrita. */
    public DocumentoPdf totales(List<Total> totales) {
        PdfPTable tabla = tablaSinBordes(60, 40);
        tabla.setWidthPercentage(50);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(10);
        for (Total t : totales) {
            Font fuente = t.destacado() ? SUBTITULO : NORMAL;
            PdfPCell etiqueta = celdaLibre(new Phrase(t.etiqueta(), fuente), Element.ALIGN_LEFT);
            PdfPCell valor = celdaLibre(new Phrase(t.valor(), fuente), Element.ALIGN_RIGHT);
            if (t.destacado()) {
                for (PdfPCell c : List.of(etiqueta, valor)) {
                    c.setBorder(Rectangle.TOP);
                    c.setBorderColor(TINTA);
                    c.setPaddingTop(5);
                }
            }
            tabla.addCell(etiqueta);
            tabla.addCell(valor);
        }
        documento.add(tabla);
        return this;
    }

    /** Texto secundario al pie del contenido. */
    public DocumentoPdf nota(String texto) {
        Paragraph p = new Paragraph(texto, TENUE);
        p.setSpacingBefore(24);
        p.setAlignment(Element.ALIGN_CENTER);
        documento.add(p);
        return this;
    }

    /** Líneas para firmar, una junto a otra. */
    public DocumentoPdf firmas(String izquierda, String derecha) {
        PdfPTable tabla = tablaSinBordes(40, 20, 40);
        tabla.setSpacingBefore(56);
        for (String texto : new String[]{izquierda, null, derecha}) {
            PdfPCell c = celdaLibre(new Phrase(texto == null ? "" : texto, TENUE), Element.ALIGN_CENTER);
            if (texto != null) {
                c.setBorder(Rectangle.TOP);
                c.setBorderColor(GRIS);
                c.setPaddingTop(4);
            }
            tabla.addCell(c);
        }
        documento.add(tabla);
        return this;
    }

    public byte[] generar() {
        documento.close();
        return salida.toByteArray();
    }

    // ------------------------------------------------------------------ formato

    public static String dinero(BigDecimal valor) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
                .format(valor == null ? BigDecimal.ZERO : valor);
    }

    /** Cantidad sin ceros sobrantes: 2.0000 -> "2", 1.5000 -> "1.5". */
    public static String cantidad(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    public static String fecha(LocalDateTime valor) {
        return valor == null ? "-" : FECHA.format(valor);
    }

    public record Total(String etiqueta, String valor, boolean destacado) {
        public Total(String etiqueta, String valor) {
            this(etiqueta, valor, false);
        }
    }

    // ------------------------------------------------------------------ celdas

    private static Font fuente(String nombre, float tamano, Color color) {
        return FontFactory.getFont(nombre, tamano, Font.NORMAL, color);
    }

    private static PdfPTable tablaSinBordes(float... anchos) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        return tabla;
    }

    private static PdfPCell celdaLibre(Phrase contenido, int alineacion) {
        PdfPCell c = new PdfPCell(contenido);
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(alineacion);
        c.setPadding(2);
        return c;
    }

    private static PdfPCell celda(Phrase contenido, boolean derecha) {
        PdfPCell c = new PdfPCell(contenido);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(LINEA);
        c.setHorizontalAlignment(derecha ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        c.setPadding(6);
        return c;
    }
}
