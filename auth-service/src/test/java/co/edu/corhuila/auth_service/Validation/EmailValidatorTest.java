package co.edu.corhuila.auth_service.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValidatorTest {

    @Test
    void validEmailsShouldPass() {
        assertDoesNotThrow(() -> EmailValidator.validateOrThrow("john.doe@gmail.com"));
        assertDoesNotThrow(() -> EmailValidator.validateOrThrow("qa_user+ops@farmanet.co"));
        assertDoesNotThrow(() -> EmailValidator.validateOrThrow("marlon-1@sub-domain.example.com"));
    }

    @Test
    void invalidDomainEndingWithHyphenShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("marlon-.@gmail-.com"));
    }

    @Test
    void invalidDomainStartingWithHyphenShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@-gmail.com"));
    }

    @Test
    void doubleAtShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@@gmail.com"));
    }

    @Test
    void emptyDomainLabelShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@gmail..com"));
    }

    @Test
    void shortTldShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@gmail.c"));
    }

    @Test
    void punycodeDomainShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@xn--gmail.com"));
    }

    @Test
    void nonAsciiDomainShouldFail() {
        assertThrows(ResponseStatusException.class,
                () -> EmailValidator.validateOrThrow("user@gmailñ.com"));
    }
}
