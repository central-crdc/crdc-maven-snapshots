package br.com.crdc.f1.commons.auth.avp;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Bridge SpEL para uso em {@code @PreAuthorize}. Registrado como bean {@code "avpSecurity"}.
 *
 * <p>Extrai o {@code sub} e {@code cognito:groups} do JWT autenticado e delega ao
 * {@link AvpAuthorizationService}. Uso tipico:</p>
 *
 * <pre>{@code
 * @PreAuthorize("@avpSecurity.can(authentication, 'f1mod:duplicata:criar', 'Duplicata', '*')")
 * public ResponseEntity<?> criar(...) { ... }
 * }</pre>
 */
public class AvpMethodSecurity {

    private final AvpAuthorizationService avpService;

    public AvpMethodSecurity(AvpAuthorizationService avpService) {
        this.avpService = avpService;
    }

    /**
     * Avalia se o usuario autenticado pode executar a acao sobre o recurso.
     *
     * @param authentication autenticacao corrente (injetada pelo SpEL como {@code authentication})
     * @param actionId       acao Cedar
     * @param resourceType   tipo Cedar do recurso
     * @param resourceId     id do recurso, ou {@code "*"}
     * @return {@code false} se nao autenticado ou principal nao-JWT; caso contrario delega ao AVP
     */
    public boolean can(
            Authentication authentication,
            String actionId,
            String resourceType,
            String resourceId) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        String sub = jwt.getSubject();
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
        if (groups == null) {
            groups = List.of();
        }

        return avpService.isAuthorized(sub, groups, actionId, resourceType, resourceId);
    }
}
