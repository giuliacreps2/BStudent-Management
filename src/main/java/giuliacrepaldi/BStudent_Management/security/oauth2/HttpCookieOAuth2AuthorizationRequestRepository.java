package giuliacrepaldi.BStudent_Management.security.oauth2;

import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

// Conserva la richiesta di autorizzazione OAuth2 (state, nonce, redirect) in un cookie firmato invece
// che nella sessione HTTP: l'applicazione resta stateless anche durante il giro su Google
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final String COOKIE_NAME = "oauth2AuthRequest";
    private static final String COOKIE_PATH = "/";
    private static final Duration COOKIE_MAX_AGE = Duration.ofMinutes(5);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // Deserializzazione limitata alle classi della richiesta OAuth2 e alle collezioni del JDK
    private static final ObjectInputFilter ALLOWED_CLASSES = ObjectInputFilter.Config.createFilter(
            "maxdepth=10;maxrefs=500;maxbytes=16384;"
                    + "org.springframework.security.oauth2.core.**;java.util.*;java.lang.*;!*");

    private final CookieUtils cookieUtils;
    private final SecretKeySpec signingKey;

    public HttpCookieOAuth2AuthorizationRequestRepository(CookieUtils cookieUtils,
                                                          @Value("${jwt.secret}") String secret) {
        this.cookieUtils = cookieUtils;
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String value = readCookie(request);
        return value == null ? null : decode(value);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            clearCookie(response);
            return;
        }
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieUtils.build(COOKIE_NAME, encode(authorizationRequest), COOKIE_PATH, COOKIE_MAX_AGE).toString());
    }

    // Monouso: letta al ritorno da Google e subito cancellata
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        clearCookie(response);
        return authorizationRequest;
    }

    // payload.firma, entrambi in Base64 URL-safe
    String encode(OAuth2AuthorizationRequest authorizationRequest) {
        byte[] payload = serialize(authorizationRequest);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(payload) + "." + encoder.encodeToString(sign(payload));
    }

    // La firma si verifica prima di deserializzare: un cookie alterato non arriva mai a readObject
    OAuth2AuthorizationRequest decode(String value) {
        try {
            int separator = value.indexOf('.');
            if (separator <= 0) {
                return null;
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] payload = decoder.decode(value.substring(0, separator));
            byte[] signature = decoder.decode(value.substring(separator + 1));
            if (!MessageDigest.isEqual(sign(payload), signature)) {
                log.warn("Cookie OAuth2 con firma non valida: ignorato");
                return null;
            }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(payload))) {
                in.setObjectInputFilter(ALLOWED_CLASSES);
                return (OAuth2AuthorizationRequest) in.readObject();
            }
        } catch (IllegalArgumentException | IOException | ClassNotFoundException | ClassCastException e) {
            log.warn("Cookie OAuth2 non leggibile: {}", e.getMessage());
            return null;
        }
    }

    private byte[] serialize(OAuth2AuthorizationRequest authorizationRequest) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(authorizationRequest);
        } catch (IOException e) {
            throw new UncheckedIOException("Serializzazione della richiesta OAuth2 non riuscita", e);
        }
        return bytes.toByteArray();
    }

    private byte[] sign(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);
            return mac.doFinal(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Firma HMAC non disponibile", e);
        }
    }

    private static String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieUtils.build(COOKIE_NAME, "", COOKIE_PATH, Duration.ZERO).toString());
    }
}
