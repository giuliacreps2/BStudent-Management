package giuliacrepaldi.BStudent_Management.security;

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

    public SecurityConfig(TokenFilter tokenFilter, @Value("${cors.allowed-origins}") List<String> allowedOrigins) {
        this.tokenFilter = tokenFilter;
        this.allowedOrigins = allowedOrigins;
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

        httpSecurity.authorizeHttpRequests(req -> req
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/import/**").hasAuthority("ADMIN")
                .requestMatchers("/roles/**").hasAuthority("ADMIN")
                .requestMatchers("/auth/register/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
        );

        return httpSecurity.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChainLogout(HttpSecurity http) throws Exception {
        http
                .logout(logout -> logout
                        .logoutUrl("/logout") // Endpoint per il logout (default /logout)
                        .logoutSuccessUrl("/login?logout") // Pagina di reindirizzamento post-logout
                        .invalidateHttpSession(true) // Invalida la sessione
                        .deleteCookies("JSESSIONID") // Cancella i cookie di sessione
                );
        return http.build();
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
