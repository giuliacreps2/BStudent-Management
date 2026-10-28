package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenToolsTest {

    private final TokenTools tokenTools = new TokenTools("test-secret-di-almeno-trentadue-caratteri-0123456789");

    @Test
    void ilTokenDiAccessoRestituisceLUtente() {
        String token = tokenTools.generateToken(User.builder().userId(42L).build());
        assertThat(tokenTools.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void ilProfiloGoogleSopravviveAlTokenDiOnboarding() {
        GoogleProfile profile = new GoogleProfile("109876543210987654321", "ada@example.com", true, "Ada", "Lovelace");
        GoogleProfile parsed = tokenTools.parseGoogleOnboardingToken(tokenTools.generateGoogleOnboardingToken(profile));
        assertThat(parsed).isEqualTo(profile);
    }

    @Test
    void ilTokenDiOnboardingNonValeComeAccessoEViceversa() {
        String onboarding = tokenTools.generateGoogleOnboardingToken(
                new GoogleProfile("123", "ada@example.com", true, "Ada", null));
        String access = tokenTools.generateToken(User.builder().userId(123L).build());

        assertThatThrownBy(() -> tokenTools.extractUserId(onboarding)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> tokenTools.parseGoogleOnboardingToken(access)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void unTokenFirmatoConAltroSegretoVieneRifiutato() {
        String foreign = new TokenTools("un-altro-segreto-lungo-almeno-trentadue-caratteri")
                .generateToken(User.builder().userId(1L).build());
        assertThatThrownBy(() -> tokenTools.extractUserId(foreign)).isInstanceOf(UnauthorizedException.class);
    }
}
