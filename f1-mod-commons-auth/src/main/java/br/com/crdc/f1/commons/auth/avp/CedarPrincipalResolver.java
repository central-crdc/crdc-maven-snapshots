package br.com.crdc.f1.commons.auth.avp;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Estrategia extensivel para construir o {@link CedarPrincipal} a partir do JWT
 * autenticado.
 *
 * <p><b>Fase 1 (default):</b> {@link CognitoGroupsPrincipalResolver} monta um
 * {@code User} com os {@code cognito:groups} como Roles (parents).</p>
 *
 * <p><b>Fase 2 (integracao com o Cadastro Centralizado):</b> o servico consumidor
 * declara seu proprio bean {@code CedarPrincipalResolver} (via
 * {@code @ConditionalOnMissingBean}) que consulta o Cadastro/banco para montar uma
 * entidade {@code Persona} com {@code Perfil.poderes} e {@code tenant_id}. A transicao
 * e um swap de bean, sem reescrever o modulo — ver ADR-LOCAL-06 no DESIGN.md.</p>
 */
public interface CedarPrincipalResolver {

    /**
     * Resolve o principal Cedar a partir do JWT.
     *
     * @param jwt token JWT autenticado (principal do {@code SecurityContext})
     * @return o principal Cedar (entidade + entidades adicionais)
     */
    CedarPrincipal resolve(Jwt jwt);
}
