package co.edu.corhuila.auth_service.Validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class NameValidator {

    private NameValidator() {
    }

    public static String normalizeAndValidateOrThrow(String rawName) {
        if (rawName == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio."
            );
        }

        String normalized = rawName.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede estar vacío."
            );
        }

        if (!normalized.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre solo puede contener letras y espacios."
            );
        }

        return normalized;
    }
}
