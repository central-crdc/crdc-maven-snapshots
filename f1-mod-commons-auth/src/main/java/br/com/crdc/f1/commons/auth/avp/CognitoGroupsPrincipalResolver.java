package br.com.crdc.f1.commons.auth.avp;

import br.com.crdc.f1.commons.auth.config.AvpProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityItem;

import java.util.List;

/**
 * Resolver default (Fase 1): monta um principal {@code <ns>::User::<sub>} com os
 * grupos {@code cognito:groups} como parents {@code <ns>::Role::<grupo>}.
 *
 * <p>O namespace vem de {@link AvpProperties#getCedarNamespace()} (default {@code F1Mod}).
 * Para o modelo Persona + Perfil (Fase 2), declare outro bean {@link CedarPrincipalResolver}
 * no servico consumidor — ver ADR-LOCAL-06 no DESIGN.md.</p>
 */
public class CognitoGroupsPrincipalResolver implements CedarPrincipalResolver {

    private final AvpProperties props;

    public CognitoGroupsPrincipalResolver(AvpProperties props) {
        this.props = props;
    }

    @Override
    public CedarPrincipal resolve(Jwt jwt) {
        String sub = jwt.getSubject();
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
        return build(sub, groups == null ? List.of() : groups);
    }

    /**
     * Constroi o principal a partir do sub e dos grupos ja extraidos. Usado tanto pelo
     * {@link #resolve(Jwt)} quanto diretamente pela implementacao de autorizacao, que
     * recebe {@code principalId}/{@code groups} no contrato publico.
     */
    public CedarPrincipal build(String principalId, java.util.Collection<String> groups) {
        String ns = props.getCedarNamespace();

        List<EntityIdentifier> parents = groups.stream()
                .map(g -> EntityIdentifier.builder()
                        .entityType(ns + "::Role")
                        .entityId(g)
                        .build())
                .toList();

        EntityItem principalEntity = EntityItem.builder()
                .identifier(EntityIdentifier.builder()
                        .entityType(ns + "::User")
                        .entityId(principalId)
                        .build())
                .parents(parents)
                .build();

        return new CedarPrincipal(principalEntity, List.of());
    }
}
