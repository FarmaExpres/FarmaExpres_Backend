package co.edu.corhuila.auth_service.Validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class EmailValidator {

    private EmailValidator() {
    }

    public static void validateOrThrow(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo es obligatorio."
            );
        }

        if (!email.equals(email.trim()) || email.contains(" ")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: no se permiten espacios."
            );
        }

        int atIndex = email.indexOf('@');
        int lastAtIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex != lastAtIndex || atIndex == email.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: debe contener una sola arroba y partes local/dominio."
            );
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        validateLocalPart(localPart);
        validateDomainPart(domainPart);
    }

    private static void validateLocalPart(String localPart) {
        if (!localPart.matches("^[A-Za-z0-9._%+\\-]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: caracteres no permitidos en la parte local."
            );
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: la parte local no puede iniciar o terminar con punto."
            );
        }

        if (localPart.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: la parte local no permite puntos consecutivos."
            );
        }
    }

    private static void validateDomainPart(String domainPart) {
        String[] labels = domainPart.split("\\.", -1);
        if (labels.length < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de correo inválido: el dominio no es válido."
            );
        }

        for (String label : labels) {
            if (label.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Formato de correo inválido: el dominio no puede tener etiquetas vacías."
                );
            }

            String lowerLabel = label.toLowerCase();
            if (lowerLabel.startsWith("xn--")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se permiten dominios internacionalizados/punycode."
                );
            }

            if (!label.matches("^[A-Za-z0-9\\-]+$")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Formato de correo inválido: el dominio contiene caracteres no permitidos."
                );
            }

            if (label.startsWith("-") || label.endsWith("-")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Las etiquetas del dominio no pueden iniciar o terminar con guion."
                );
            }
        }

        String tld = labels[labels.length - 1];
        if (!tld.matches("^[A-Za-z]{2,}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El dominio de nivel superior debe tener solo letras y mínimo 2 caracteres."
            );
        }
    }
}
