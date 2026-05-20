package co.edu.corhuila.inventory_service.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;


@Service
public class JwtService {

    private final String secret;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
