package br.com.crdc.f1.commons.auth.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Guard fail-closed: impede o startup inseguro em producao.
 *
 * <p>Ativa apenas no profile {@code prod}. No {@code @PostConstruct} valida as
 * invariantes de seguranca; qualquer violacao lanca {@link IllegalStateException}
 * com mensagem instrutiva (propriedade + env var), abortando o boot.</p>
 */
@Configuration
@Profile("prod")
public class AuthProdSafetyGuard {

    private static final Logger log = LoggerFactory.getLogger(AuthProdSafetyGuard.class);

    @Value("${crdc.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${crdc.avp.enabled:false}")
    private boolean avpEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String jwtIssuerUri;

    @PostConstruct
    public void validateProdSecurityInvariants() {
        if (!securityEnabled) {
            throw new IllegalStateException(
                    "[fail-closed] Profile 'prod' ativo mas 'crdc.security.enabled=false'. "
                    + "Corrija: SECURITY_ENABLED=true.");
        }
        if (!avpEnabled) {
            throw new IllegalStateException(
                    "[fail-closed] Profile 'prod' ativo mas 'crdc.avp.enabled=false'. "
                    + "Corrija: AVP_ENABLED=true + AVP_POLICY_STORE_ID=<id>.");
        }
        if (jwtIssuerUri == null || jwtIssuerUri.isBlank()) {
            throw new IllegalStateException(
                    "[fail-closed] Profile 'prod' sem JWT issuer-uri. "
                    + "Corrija: JWT_ISSUER_URI=https://cognito-idp.<region>.amazonaws.com/<pool-id>.");
        }
        log.info("[fail-closed] Invariantes de seguranca de producao OK: "
                + "security + AVP habilitados, issuer-uri presente.");
    }
}
