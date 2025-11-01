package dev.kuku.authsome.config;

import dev.kuku.authsome.config.securityFilter.AuthsomeTenantApiKeyFilter;
import dev.kuku.authsome.config.securityFilter.AuthsomeTenantJwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthsomeTenantJwtFilter tenantJwtFilter;
    private final AuthsomeTenantApiKeyFilter tenantApiKeyFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**", "/health", "/actuator/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tenantApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}