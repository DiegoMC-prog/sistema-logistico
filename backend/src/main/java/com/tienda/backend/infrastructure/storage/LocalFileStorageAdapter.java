package com.tienda.backend.infrastructure.storage;

import com.tienda.backend.application.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageAdapter implements FileStorageService {

    private final Path rootLocation;

    private static final List<String> EXTENSIONES_PERMITIDAS = List.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    private static final List<String> TIPOS_MIME_PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    public LocalFileStorageAdapter(@Value("${app.storage.location:uploads}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento: " + this.rootLocation, e);
        }
    }

    @Override
    public String almacenar(MultipartFile archivo, String subdirectorio) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se puede almacenar un archivo vacío.");
        }

        String nombreOriginal = StringUtils.cleanPath(
                archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "archivo"
        );

        validarArchivo(archivo, nombreOriginal);

        String extension = obtenerExtension(nombreOriginal);
        String nombreUnico = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        try {
            Path destinoDirectorio = (subdirectorio != null && !subdirectorio.isBlank())
                    ? this.rootLocation.resolve(subdirectorio).normalize()
                    : this.rootLocation;

            // Protección contra Path Traversal
            if (!destinoDirectorio.startsWith(this.rootLocation)) {
                throw new IllegalArgumentException("Ruta de destino inválida.");
            }

            Files.createDirectories(destinoDirectorio);

            Path archivoDestino = destinoDirectorio.resolve(nombreUnico).normalize();
            if (!archivoDestino.startsWith(this.rootLocation)) {
                throw new IllegalArgumentException("Ruta de archivo destino inválida.");
            }

            try (InputStream inputStream = archivo.getInputStream()) {
                Files.copy(inputStream, archivoDestino, StandardCopyOption.REPLACE_EXISTING);
            }

            String subPath = (subdirectorio != null && !subdirectorio.isBlank())
                    ? subdirectorio + "/" + nombreUnico
                    : nombreUnico;

            // Retornar la ruta web pública (/uploads/...)
            return "/uploads/" + subPath.replace("\\", "/");

        } catch (IOException e) {
            log.error("Error al almacenar el archivo {}: {}", nombreOriginal, e.getMessage(), e);
            throw new RuntimeException("Error al almacenar el archivo en disco.", e);
        }
    }

    @Override
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }

        try {
            String limpia = rutaRelativa.trim();
            if (limpia.startsWith("/uploads/")) {
                limpia = limpia.substring("/uploads/".length());
            } else if (limpia.startsWith("uploads/")) {
                limpia = limpia.substring("uploads/".length());
            }

            Path archivoAEliminar = this.rootLocation.resolve(limpia).normalize();

            // Protección contra Path Traversal
            if (archivoAEliminar.startsWith(this.rootLocation) && Files.exists(archivoAEliminar)) {
                Files.delete(archivoAEliminar);
                log.info("Archivo eliminado correctamente: {}", archivoAEliminar);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", rutaRelativa, e.getMessage());
        }
    }

    private void validarArchivo(MultipartFile archivo, String nombreOriginal) {
        String extension = obtenerExtension(nombreOriginal).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida (." + extension + "). Extensiones válidas: " + EXTENSIONES_PERMITIDAS);
        }

        String contentType = archivo.getContentType();
        if (contentType != null && !TIPOS_MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de contenido no permitido (" + contentType + "). Debe ser una imagen válida.");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int indicePunto = nombreArchivo.lastIndexOf('.');
        if (indicePunto >= 0 && indicePunto < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(indicePunto + 1);
        }
        return "";
    }
}
