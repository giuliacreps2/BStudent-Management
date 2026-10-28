package giuliacrepaldi.BStudent_Management.security.oauth2;

import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private static final String SECRET = "test-secret-di-almeno-trentadue-caratteri-0123456789";

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HttpCookieOAuth2AuthorizationRequestRepository(new CookieUtils(), SECRET);
    }

    @Test
    void salvaERileggeLaRichiestaDalCookie() {
        OAuth2AuthorizationRequest original = googleRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveAuthorizationRequest(original, new MockHttpServletRequest(), response);
        Cookie cookie = response.getCookie(HttpCookieOAuth2AuthorizationRequestRepository.COOKIE_NAME);

        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        // I browser scartano i cookie oltre ~4096 byte
        assertThat(response.getHeader("Set-Cookie").length()).isLessThan(4096);

        MockHttpServletRequest callback = new MockHttpServletRequest();
        callback.setCookies(cookie);
        MockHttpServletResponse callbackResponse = new MockHttpServletResponse();
        OAuth2AuthorizationRequest loaded = repository.removeAuthorizationRequest(callback, callbackResponse);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getState()).isEqualTo(original.getState());
        assertThat(loaded.getRedirectUri()).isEqualTo(original.getRedirectUri());
        assertThat(loaded.getScopes()).isEqualTo(original.getScopes());
        assertThat(loaded.<String>getAttribute(OidcParameterNames.NONCE)).isEqualTo("nonce-originale");
        assertThat(loaded.getAuthorizationRequestUri()).isEqualTo(original.getAuthorizationRequestUri());
        // Monouso: dopo la lettura il cookie viene cancellato
        assertThat(callbackResponse.getCookie(HttpCookieOAuth2AuthorizationRequestRepository.COOKIE_NAME).getMaxAge())
                .isZero();
    }

    @Test
    void rifiutaUnCookieConFirmaAlterata() {
        String value = repository.encode(googleRequest());
        String payload = value.substring(0, value.indexOf('.'));
        // Stesso payload, firma di un altro segreto
        String forged = new HttpCookieOAuth2AuthorizationRequestRepository(new CookieUtils(),
                "un-altro-segreto-lungo-almeno-trentadue-caratteri").encode(googleRequest());
        String tampered = payload + forged.substring(forged.indexOf('.'));

        assertThat(repository.decode(tampered)).isNull();
        assertThat(repository.decode("non-un-cookie-valido")).isNull();
        assertThat(repository.decode(value)).isNotNull();
    }

    @Test
    void ignoraLaRichiestaSenzaCookie() {
        assertThat(repository.loadAuthorizationRequest(new MockHttpServletRequest())).isNull();
    }

    private static OAuth2AuthorizationRequest googleRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client-id-di-test.apps.googleusercontent.com")
                .redirectUri("http://localhost:8081/login/oauth2/code/google")
                .scopes(Set.of("openid", "profile", "email"))
                .state("state-casuale-0123456789")
                .additionalParameters(Map.of(OidcParameterNames.NONCE, "hash-del-nonce"))
                .attributes(Map.of(OAuth2ParameterNames.REGISTRATION_ID, "google", OidcParameterNames.NONCE,
                        "nonce-originale"))
                .build();
    }
}
