package co.edu.corhuila.auth_service.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameValidatorTest {

    @Test
    void shouldAcceptValidNames() {
        assertEquals("Nicolas Tello",
                NameValidator.normalizeAndValidateOrThrow("Nicolas Tello"));
        assertEquals("Nicolás Tello",
                NameValidator.normalizeAndValidateOrThrow("  Nicolás   Tello  "));
        assertEquals("MARÍA JOSÉ",
                NameValidator.normalizeAndValidateOrThrow("MARÍA JOSÉ"));
        assertEquals("José Ñúñez",
                NameValidator.normalizeAndValidateOrThrow("José Ñúñez"));
    }

    @Test
    void shouldRejectNamesWithNumbers() {
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("Nicolas123"));
    }

    @Test
    void shouldRejectNamesWithSpecialCharacters() {
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("Marlon_Romero"));
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("Juan-Perez"));
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("Pedro."));
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("Lina@Empresa"));
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("!!!"));
    }

    @Test
    void shouldRejectBlankNames() {
        assertThrows(ResponseStatusException.class,
                () -> NameValidator.normalizeAndValidateOrThrow("   "));
    }
}
