package br.com.crdc.f1.commons.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propriedades de autenticacao (prefixo {@code crdc.security}).
 *
 * <p>Controla a ativacao da cadeia de filtros JWT (Cognito) e a lista de URL patterns
 * liberados sem autenticacao (merge com os defaults do modulo).</p>
 */
@ConfigurationProperties(prefix = "crdc.security")
public class AuthProperties {

    /**
     * Master switch — ativa a {@code SecurityFilterChain} JWT (resource server).
     * Env var: {@code SECURITY_ENABLED}. Default: {@code false}.
     */
    private boolean enabled = false;

    /**
     * URL patterns adicionais liberados sem autenticacao. Sao adicionados (merge)
     * aos defaults do modulo (actuator health/info/prometheus), nunca os substituem.
     * Env var: {@code SECURITY_PERMIT_PATTERNS}.
     */
    private List<String> permitPatterns = List.of();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPermitPatterns() {
        return permitPatterns;
    }

    public void setPermitPatterns(List<String> permitPatterns) {
        this.permitPatterns = permitPatterns == null ? List.of() : permitPatterns;
    }
}
