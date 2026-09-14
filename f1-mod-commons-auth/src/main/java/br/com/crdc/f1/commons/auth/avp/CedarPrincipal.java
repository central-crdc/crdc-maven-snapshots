package br.com.crdc.f1.commons.auth.avp;

import software.amazon.awssdk.services.verifiedpermissions.model.EntityItem;

import java.util.List;

/**
 * Principal Cedar resolvido a partir do JWT, pronto para uso em
 * {@code IsAuthorizedRequest}.
 *
 * <p>Na Fase 1 (modelo User + Roles) o {@code entity} e um {@code User} com os grupos
 * Cognito como parents (Roles) e {@code additionalEntities} vazio. Na Fase 2 (modelo
 * Persona + Perfil do Cadastro Centralizado) o {@code entity} pode ser uma {@code Persona}
 * e {@code additionalEntities} podem carregar entidades relacionadas (Perfil, etc.).</p>
 *
 * @param entity             a entidade principal (User ou Persona)
 * @param additionalEntities entidades adicionais a incluir no request (pode ser vazia)
 */
public record CedarPrincipal(
        EntityItem entity,
        List<EntityItem> additionalEntities) {

    public CedarPrincipal {
        additionalEntities = additionalEntities == null ? List.of() : List.copyOf(additionalEntities);
    }
}
