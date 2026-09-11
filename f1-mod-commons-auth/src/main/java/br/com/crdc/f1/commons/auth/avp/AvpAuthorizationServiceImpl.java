package br.com.crdc.f1.commons.auth.avp;

import br.com.crdc.f1.commons.auth.config.AvpProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.verifiedpermissions.VerifiedPermissionsClient;
import software.amazon.awssdk.services.verifiedpermissions.model.ActionIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.Decision;
import software.amazon.awssdk.services.verifiedpermissions.model.EntitiesDefinition;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityItem;
import software.amazon.awssdk.services.verifiedpermissions.model.IsAuthorizedRequest;
import software.amazon.awssdk.services.verifiedpermissions.model.IsAuthorizedResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementacao real do {@link AvpAuthorizationService} usando o AWS SDK v2.
 *
 * <p>A construcao do principal Cedar e delegada ao {@link CedarPrincipalResolver}
 * (ADR-LOCAL-06), permitindo o swap para o modelo Persona + Perfil na Fase 2 sem
 * alterar esta classe. O namespace, policy store e demais parametros vem de
 * {@link AvpProperties}.</p>
 *
 * <p>Fail-safe: qualquer {@link SdkException} resulta em {@code false} (deny-by-default,
 * CWE-703). Tokens e PII nunca sao logados.</p>
 */
public class AvpAuthorizationServiceImpl implements AvpAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AvpAuthorizationServiceImpl.class);

    private final VerifiedPermissionsClient avpClient;
    private final AvpProperties props;
    private final CognitoGroupsPrincipalResolver principalBuilder;

    public AvpAuthorizationServiceImpl(VerifiedPermissionsClient avpClient, AvpProperties props) {
        this.avpClient = avpClient;
        this.props = props;
        // Builder default para montar a entidade principal a partir de principalId + groups.
        // A Fase 2 pode sobrescrever o CedarPrincipalResolver bean, mas o contrato publico
        // isAuthorized(principalId, groups, ...) mantem a construcao consistente aqui.
        this.principalBuilder = new CognitoGroupsPrincipalResolver(props);
    }

    @PostConstruct
    void validateConfiguration() {
        if (props.getPolicyStoreId() == null || props.getPolicyStoreId().isBlank()) {
            throw new IllegalStateException(
                    "AVP habilitado (crdc.avp.enabled=true) mas policy-store-id nao configurado. "
                    + "Configure a env var AVP_POLICY_STORE_ID com o ID do policy store.");
        }
    }

    @Override
    public boolean isAuthorized(
            String principalId,
            Collection<String> groups,
            String actionId,
            String resourceType,
            String resourceId) {
        try {
            String ns = props.getCedarNamespace();

            CedarPrincipal principal = principalBuilder.build(
                    principalId, groups == null ? List.of() : groups);

            List<EntityItem> entities = new ArrayList<>();
            entities.add(principal.entity());
            entities.addAll(principal.additionalEntities());

            IsAuthorizedRequest request = IsAuthorizedRequest.builder()
                    .policyStoreId(props.getPolicyStoreId())
                    .principal(principal.entity().identifier())
                    .action(ActionIdentifier.builder()
                            .actionType(ns + "::Action")
                            .actionId(actionId)
                            .build())
                    .resource(EntityIdentifier.builder()
                            .entityType(ns + "::" + resourceType)
                            .entityId(resourceId)
                            .build())
                    .entities(EntitiesDefinition.builder()
                            .entityList(entities)
                            .build())
                    .build();

            IsAuthorizedResponse response = avpClient.isAuthorized(request);
            boolean allowed = Decision.ALLOW.equals(response.decision());

            log.debug("AVP IsAuthorized: principal={} action={} resource={}/{} decision={}",
                    principalId, actionId, resourceType, resourceId, response.decision());

            return allowed;

        } catch (Exception e) {
            log.error("AVP IsAuthorized falhou para principal={} action={}: {}",
                    principalId, actionId, e.getMessage(), e);
            return false;
        }
    }
}
