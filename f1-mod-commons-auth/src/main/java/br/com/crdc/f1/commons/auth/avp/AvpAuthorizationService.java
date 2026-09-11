package br.com.crdc.f1.commons.auth.avp;

import java.util.Collection;

/**
 * Contrato de autorizacao fine-grained via AWS Verified Permissions (Cedar).
 *
 * <p>Avalia se um principal (usuario Cognito + grupos) pode executar uma acao
 * sobre um recurso. A implementacao real ({@code AvpAuthorizationServiceImpl})
 * delega ao AVP; o fallback ({@code PermissiveAvpAuthorizationService}) libera tudo
 * em dev/test.</p>
 *
 * <p>O contrato e identico ao usado hoje no {@code f1-mod-duplicatas}, para migracao
 * transparente.</p>
 */
public interface AvpAuthorizationService {

    /**
     * Avalia se o principal pode executar a acao sobre o recurso.
     *
     * @param principalId  Cognito sub (claim {@code sub} do JWT)
     * @param groups       grupos Cognito (claim {@code cognito:groups})
     * @param actionId     acao Cedar (ex: {@code "f1mod:duplicata:criar"})
     * @param resourceType tipo Cedar do recurso (ex: {@code "Duplicata"})
     * @param resourceId   id do recurso, ou {@code "*"} para acoes genericas
     * @return {@code true} se a decisao for ALLOW; {@code false} se DENY ou em caso de erro
     */
    boolean isAuthorized(
            String principalId,
            Collection<String> groups,
            String actionId,
            String resourceType,
            String resourceId);
}
