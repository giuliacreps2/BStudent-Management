package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleProfile;
import io.jsonwebtoken.Claims;
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
    private static final Duration GOOGLE_ONBOARDING_DURATION = Duration.ofMinutes(15);

    // Ogni JWT dichiara il proprio uso: un token di onboarding non vale come accesso e viceversa
    private static final String TYPE_CLAIM = "typ";
    private static final String ACCESS_TYPE = "access";
    private static final String GOOGLE_ONBOARDING_TYPE = "google_onboarding";

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
                .claim(TYPE_CLAIM, ACCESS_TYPE)
                .signWith(key)
                .compact();
    }

    //TO READ TOKEN with ID
    public Long extractUserId(String token) {
        try {
            return Long.parseLong(parse(token, ACCESS_TYPE).getSubject());
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    public Duration getTokenDuration() {
        return TOKEN_DURATION;
    }

    //GOOGLE ONBOARDING: profilo Google firmato tra il ritorno da Google e il completamento della registrazione.
    // Firmato ma non cifrato: contiene solo email e nome, viaggia in un cookie httpOnly di breve durata
    public String generateGoogleOnboardingToken(GoogleProfile profile) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(GOOGLE_ONBOARDING_DURATION)))
                .subject(profile.subject())
                .claim(TYPE_CLAIM, GOOGLE_ONBOARDING_TYPE)
                .claim("email", profile.email())
                .claim("given_name", profile.givenName())
                .claim("family_name", profile.familyName())
                .signWith(key)
                .compact();
    }

    // Solo profili con email verificata ottengono il token: emailVerified è sempre true
    public GoogleProfile parseGoogleOnboardingToken(String token) {
        try {
            Claims claims = parse(token, GOOGLE_ONBOARDING_TYPE);
            return new GoogleProfile(claims.getSubject(), claims.get("email", String.class), true,
                    claims.get("given_name", String.class), claims.get("family_name", String.class));
        } catch (Exception e) {
            throw new UnauthorizedException("Registrazione con Google scaduta: accedi di nuovo con Google");
        }
    }

    public Duration getGoogleOnboardingDuration() {
        return GOOGLE_ONBOARDING_DURATION;
    }

    private Claims parse(String token, String expectedType) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
        if (!expectedType.equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Tipo di token non valido");
        }
        return claims;
    }
}
