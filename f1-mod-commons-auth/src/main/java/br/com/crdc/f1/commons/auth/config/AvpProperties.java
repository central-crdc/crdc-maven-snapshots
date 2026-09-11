package br.com.crdc.f1.commons.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Propriedades de autorizacao AVP/Cedar (prefixo {@code crdc.avp}).
 *
 * <p>Configura a integracao com o AWS Verified Permissions: policy store, namespace
 * Cedar e timeout das chamadas {@code IsAuthorized}. Cada dominio (F1-Mod, Cadastro
 * Centralizado) mantem seu proprio policy store — ver ADR-LOCAL-07 no DESIGN.md.</p>
 */
@ConfigurationProperties(prefix = "crdc.avp")
public class AvpProperties {

    /**
     * Ativa a autorizacao AVP real (bean {@code AvpAuthorizationServiceImpl}).
     * Env var: {@code AVP_ENABLED}. Default: {@code false}.
     */
    private boolean enabled = false;

    /**
     * ID do policy store no AWS Verified Permissions.
     * Env var: {@code AVP_POLICY_STORE_ID}. Obrigatorio quando {@code enabled=true}.
     */
    private String policyStoreId;

    /**
     * Namespace Cedar usado para prefixar os tipos de entidade e acao
     * (ex: {@code F1Mod::User}, {@code F1Mod::Action}).
     * Env var: {@code AVP_CEDAR_NAMESPACE}. Default: {@code F1Mod}.
     */
    private String cedarNamespace = "F1Mod";

    /**
     * Timeout das chamadas {@code IsAuthorized}.
     * Env var: {@code AVP_TIMEOUT}. Default: {@code 2s}.
     */
    private Duration timeout = Duration.ofSeconds(2);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPolicyStoreId() {
        return policyStoreId;
    }

    public void setPolicyStoreId(String policyStoreId) {
        this.policyStoreId = policyStoreId;
    }

    public String getCedarNamespace() {
        return cedarNamespace;
    }

    public void setCedarNamespace(String cedarNamespace) {
        this.cedarNamespace = cedarNamespace;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
