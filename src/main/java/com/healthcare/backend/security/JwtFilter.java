package com.healthcare.backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.healthcare.backend.entity.Account;
import com.healthcare.backend.repository.AccountRepository;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtServiceInterface jwtService;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        log.info("JWT filter request: method={}, uri={}, hasAuthorizationHeader={}",
                request.getMethod(),
                request.getRequestURI(),
                authHeader != null);

        if (authHeader == null || authHeader.isBlank()) {
            request.setAttribute("authError", "Missing Authorization Bearer token");
        } else if (!authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            request.setAttribute("authError", "Authorization header must start with Bearer");
            log.info("JWT filter skipped bearer parsing: uri={}, headerValue={}",
                    request.getRequestURI(),
                    authHeader);
        } else {
            String token = normalizeToken(authHeader.substring(7));
            boolean validToken = !token.isBlank() && jwtService.validateToken(token);

            log.info("JWT filter token state: uri={}, tokenPresent={}, validToken={}",
                    request.getRequestURI(),
                    !token.isBlank(),
                    validToken);

            if (token.isBlank()) {
                request.setAttribute("authError", "Bearer token is blank");
            } else if (!validToken) {
                request.setAttribute("authError", "Invalid or expired token");
            } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    String email = jwtService.extractEmail(token);
                    Long accountId = jwtService.extractAccountId(token);
                    Account account = accountRepository.findWithRoleByEmail(email).orElse(null);
                    String roleName = account != null && account.getRole() != null
                            ? account.getRole().getRoleName()
                            : jwtService.extractRole(token);
                    String authorityName = roleName != null && roleName.startsWith("ROLE_")
                            ? roleName
                            : "ROLE_" + roleName;

                    List<GrantedAuthority> authorities = buildAuthorities(authorityName);

                    UserPrincipal principal = new UserPrincipal(accountId, email, roleName);

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    context.setAuthentication(auth);

                    SecurityContextHolder.setContext(context);
                    log.info("JWT filter authenticated: email={}, authority={}", email, authorityName);
                } catch (RuntimeException ex) {
                    request.setAttribute("authError", "Cannot read authentication data from token: " + ex.getMessage());
                    log.warn("JWT filter failed to build authentication", ex);
                }
            }
        }
        
        filterChain.doFilter(request, response);
        return;
    }

    private String normalizeToken(String token) {
        String normalizedToken = token == null ? "" : token.trim();

        if (normalizedToken.startsWith("Bearer ")) {
            normalizedToken = normalizedToken.substring(7).trim();
        }

        if (normalizedToken.length() >= 2
                && normalizedToken.startsWith("\"")
                && normalizedToken.endsWith("\"")) {
            normalizedToken = normalizedToken.substring(1, normalizedToken.length() - 1).trim();
        }

        return normalizedToken;
    }

    private List<GrantedAuthority> buildAuthorities(String authorityName) {
        if ("ROLE_ADMIN".equals(authorityName)) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_RECEPTIONIST"),
                    new SimpleGrantedAuthority("ROLE_DOCTOR"),
                    new SimpleGrantedAuthority("ROLE_PATIENT"),
                    new SimpleGrantedAuthority("ROLE_ACCOUNTANT")
            );
        }

        return List.of(new SimpleGrantedAuthority(authorityName));
    }
}
