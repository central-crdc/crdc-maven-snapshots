package br.com.crdc.f1.commons.auth.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import br.com.crdc.f1.commons.auth.jwt.CognitoGroupsAuthenticationConverter;

/**
 * Auto-configuration da cadeia de filtros JWT (Cognito resource server).
 *
 * <p>Ativa quando {@code crdc.security.enabled=true}. Registra:</p>
 * <ul>
 *   <li>{@code SecurityFilterChain} STATELESS, CSRF off, com validacao JWT via issuer-uri;</li>
 *   <li>{@code JwtAuthenticationConverter} que mapeia {@code cognito:groups} -> authorities.</li>
 * </ul>
 *
 * <p>O method security ({@code @PreAuthorize}) e habilitado separadamente pela
 * {@link MethodSecurityAutoConfiguration}, independente desta cadeia.</p>
 *
 * <p>Os patterns default (actuator health/info/prometheus) sao sempre liberados; os
 * {@code crdc.security.permit-patterns} sao adicionados (merge), nunca substituem.</p>
 */
@Configuration
@ConditionalOnProperty(name = "crdc.security.enabled", havingValue = "true")
@EnableWebSecurity
public class JwtSecurityAutoConfiguration {

    private static final String[] DEFAULT_PERMIT_PATTERNS = {
            "/actuator/health", "/actuator/health/**",
            "/actuator/info", "/actuator/prometheus"
    };

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain jwtFilterChain(
            HttpSecurity http,
            AuthProperties authProps,
            JwtAuthenticationConverter jwtAuthConverter) throws Exception {

        List<String> allPermits = new ArrayList<>(List.of(DEFAULT_PERMIT_PATTERNS));
        allPermits.addAll(authProps.getPermitPatterns());

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    allPermits.forEach(p -> auth.requestMatchers(p).permitAll());
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationConverter.class)
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new CognitoGroupsAuthenticationConverter().build();
    }
}
