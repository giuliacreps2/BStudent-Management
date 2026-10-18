package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.services.identity.RolesService;
import giuliacrepaldi.BStudent_Management.services.identity.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "accessToken";

    private final TokenTools tokenTools;
    private final UsersService usersService;
    private final RolesService rolesService;

    public TokenFilter(TokenTools tokenTools, UsersService usersService, RolesService rolesService) {
        this.tokenTools = tokenTools;
        this.usersService = usersService;
        this.rolesService = rolesService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractTokenFromCookies(request);

        if (accessToken == null || accessToken.isEmpty()
                || accessToken.equalsIgnoreCase("null") || accessToken.equalsIgnoreCase("undefined")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = tokenTools.extractUserId(accessToken);
            User user = usersService.findById(userId);

            List<Role> listRoles = rolesService.findByUserId(userId);
            List<SimpleGrantedAuthority> authorities = listRoles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getCode()))
                    .toList();

            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("Token valido - utente autenticato: {}", userId);
        } catch (Exception e) {
            // Token non valido/scaduto/utente non trovato: procedi come richiesta anonima.
            // Sarà Spring Security (o il controller) a rispondere 401/403 se la rotta è protetta.
            log.debug("Token non valido per {}: {}", request.getServletPath(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String servletPath = request.getServletPath();
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        return antPathMatcher.match("/auth/login", servletPath)
                || antPathMatcher.match("/auth/logout", servletPath);
//                || antPathMatcher.match("/auth/verify/**", servletPath);
    }

}
