package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class TokenTools {

    private static final Duration TOKEN_DURATION = Duration.ofDays(7);

    private final SecretKey key;

    public TokenTools(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //TO GENERATE TOKEN
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_DURATION)))
                .subject(String.valueOf(user.getUserId()))
                .signWith(key)
                .compact();
    }

    //TO READ TOKEN with ID
    public Long extractUserId(String token) {
        try {
            String subject = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(subject);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    public Duration getTokenDuration() {
        return TOKEN_DURATION;
    }
}
