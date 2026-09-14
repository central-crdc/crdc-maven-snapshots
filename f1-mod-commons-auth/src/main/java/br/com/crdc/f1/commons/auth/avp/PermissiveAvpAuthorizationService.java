package br.com.crdc.f1.commons.auth.avp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Fallback permissivo do {@link AvpAuthorizationService} para dev/test.
 *
 * <p>Sempre retorna {@code true} (permite tudo) e loga em DEBUG. Registrado apenas
 * fora do profile {@code prod} e quando a implementacao real ({@code AvpAuthorizationServiceImpl})
 * nao esta presente — ver {@code AvpAutoConfiguration} / {@code AuthAutoConfiguration}.</p>
 */
public class PermissiveAvpAuthorizationService implements AvpAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(PermissiveAvpAuthorizationService.class);

    @Override
    public boolean isAuthorized(
            String principalId,
            Collection<String> groups,
            String actionId,
            String resourceType,
            String resourceId) {
        log.debug("[PermissiveAVP] Permitindo action={} resource={}/{} para principal={}",
                actionId, resourceType, resourceId, principalId);
        return true;
    }
}
