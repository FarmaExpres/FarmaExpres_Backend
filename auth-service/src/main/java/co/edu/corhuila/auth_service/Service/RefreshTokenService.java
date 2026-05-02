package co.edu.corhuila.auth_service.Service;

import co.edu.corhuila.auth_service.Entity.RefreshToken;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration-ms:86400000}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken(
                user,
                hashToken(rawToken),
                Instant.now().plusMillis(refreshExpirationMs)
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshTokenIssue rotateRefreshToken(String rawToken) {
        RefreshToken refreshToken = findValidRefreshToken(rawToken);
        refreshToken.markUsed();
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String newRawToken = createRefreshToken(refreshToken.getUser());
        return new RefreshTokenIssue(refreshToken.getUser(), newRawToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        RefreshToken refreshToken = findValidRefreshToken(rawToken);
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken findValidRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token requerido");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalido"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revocado");
        }

        if (refreshToken.isExpired()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        return refreshToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    public record RefreshTokenIssue(User user, String refreshToken) {
    }
}
