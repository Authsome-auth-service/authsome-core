package dev.kuku.authsome.config.securityFilter;

import dev.kuku.authsome.orchestrator.TenantCoordinator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This web filter attempts to parse and get tenant information from API keys present in incoming requests by accessing the API-Tenant header.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthsomeTenantApiKeyFilter extends OncePerRequestFilter {
    final TenantCoordinator tenantCoordinator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Skip if already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = request.getHeader("API-Tenant");
            if (apiKey == null) {
                return;
            }
            var fetched = tenantCoordinator.getTenantFromApi(apiKey);
            if (fetched != null) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(fetched, null));
            }
            //TODO attach role that will allow tenant to access everything under its own domain
            //TODO when we have project level api key we can modify the roles to only allow project level access
        } catch (Exception e) {
            log.error("Error while setting up auth security context", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
