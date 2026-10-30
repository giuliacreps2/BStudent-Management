package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.security.oauth2.GoogleLoginFailureHandler;
import giuliacrepaldi.BStudent_Management.security.oauth2.GoogleLoginSuccessHandler;
import giuliacrepaldi.BStudent_Management.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenFilter tokenFilter;
    private final List<String> allowedOrigins;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final GoogleLoginSuccessHandler googleLoginSuccessHandler;
    private final GoogleLoginFailureHandler googleLoginFailureHandler;

    public SecurityConfig(TokenFilter tokenFilter,
                          @Value("${cors.allowed-origins}") List<String> allowedOrigins,
                          HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository,
                          GoogleLoginSuccessHandler googleLoginSuccessHandler,
                          GoogleLoginFailureHandler googleLoginFailureHandler) {
        this.tokenFilter = tokenFilter;
        this.allowedOrigins = allowedOrigins;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.googleLoginSuccessHandler = googleLoginSuccessHandler;
        this.googleLoginFailureHandler = googleLoginFailureHandler;
    }

    // TokenFilter è un @Component: senza questo bean Spring Boot lo registrerebbe anche come filtro
    // del servlet container, fuori dalla security chain
    @Bean
    public FilterRegistrationBean<TokenFilter> tokenFilterRegistration(TokenFilter tokenFilter) {
        FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<>(tokenFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"Not authenticated\"}");
                })
        );

        httpSecurity.formLogin(formLogin -> formLogin.disable());
        httpSecurity.csrf(csrf -> csrf.disable());

        // Google OAuth2/OIDC: avvio su /oauth2/authorization/google, ritorno su /login/oauth2/code/google.
        // Richiesta di autorizzazione in un cookie firmato (niente sessione); l'esito diventa il nostro JWT
        httpSecurity.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                        .authorizationRequestRepository(authorizationRequestRepository))
                .successHandler(googleLoginSuccessHandler)
                .failureHandler(googleLoginFailureHandler)
        );

        httpSecurity.authorizeHttpRequests(req -> req
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // TODO (billing, punto 1): webhook Stripe pubblico, autenticato dalla firma (Stripe-Signature)
                .requestMatchers("/api/import/**").hasAuthority("ADMIN")
                .requestMatchers("/roles/**").hasAuthority("ADMIN")
                .requestMatchers("/auth/me", "/auth/me/**").authenticated()
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
        );

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
