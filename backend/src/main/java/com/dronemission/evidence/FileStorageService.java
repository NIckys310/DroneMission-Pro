package com.dronemission.evidence;

import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Guarda los archivos de evidencia en una carpeta local (configurable con STORAGE_PATH). */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "mp4", "mov", "webm", "pdf", "doc", "docx",
            "xls", "xlsx", "txt", "csv", "zip", "kml", "tif", "tiff");

    private final Path root;

    public FileStorageService(@Value("${app.storage.path}") String storagePath) throws IOException {
        this.root = Path.of(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Guarda el archivo con un nombre aleatorio y devuelve ese nombre. */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("El archivo está vacío");
        }
        String original = file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessRuleException("Tipo de archivo no permitido (." + extension + "). Permitidos: " + ALLOWED_EXTENSIONS);
        }
        String storedName = UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo", e);
        }
        return storedName;
    }

    public Resource load(String storedName) {
        try {
            Path file = root.resolve(storedName).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) {
                throw new ResourceNotFoundException("El archivo de la evidencia no existe");
            }
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("El archivo de la evidencia no existe");
        }
    }

    public String contentType(String storedName) {
        try {
            String type = Files.probeContentType(root.resolve(storedName));
            return type != null ? type : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(root.resolve(storedName).normalize());
        } catch (IOException ignored) {
            // si no se puede borrar el archivo físico no se interrumpe la operación
        }
    }
}
