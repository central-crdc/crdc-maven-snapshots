package br.com.crdc.f1.commons.auth.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import br.com.crdc.f1.commons.auth.avp.AvpAuthorizationService;
import br.com.crdc.f1.commons.auth.avp.AvpMethodSecurity;
import br.com.crdc.f1.commons.auth.avp.PermissiveAvpAuthorizationService;

/**
 * Entry point (umbrella) da auto-configuration do {@code f1-mod-commons-auth}.
 *
 * <p>Registrada no {@code META-INF/spring/...AutoConfiguration.imports}. Importa as
 * sub-configuracoes (cada uma com suas proprias condicoes) e provê os fallbacks
 * permissivos de AVP para dev/test.</p>
 *
 * <p>Comportamento resultante:</p>
 * <ul>
 *   <li>{@code crdc.security.enabled=true} -> cadeia JWT + method security;</li>
 *   <li>{@code crdc.security.enabled=false} + nao-prod -> cadeia permitAll;</li>
 *   <li>{@code crdc.avp.enabled=true} + SDK -> autorizacao AVP real;</li>
 *   <li>{@code crdc.avp.enabled=false} + nao-prod -> AVP permissivo (permite tudo);</li>
 *   <li>profile {@code prod} com config incorreta -> startup abortado (fail-closed).</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties({AuthProperties.class, AvpProperties.class})
@Import({
        MethodSecurityAutoConfiguration.class,
        JwtSecurityAutoConfiguration.class,
        PermissiveSecurityAutoConfiguration.class,
        AvpAutoConfiguration.class,
        AuthProdSafetyGuard.class
})
public class AuthAutoConfiguration {

    /**
     * Fallback permissivo de autorizacao: usado quando nao ha implementacao real de
     * {@link AvpAuthorizationService} (ex: {@code crdc.avp.enabled=false}) e o profile
     * NAO e {@code prod}. Fica fora da {@link AvpAutoConfiguration} de proposito, pois
     * aquela config so carrega quando AVP esta habilitado.
     */
    @Bean
    @Profile("!prod")
    @ConditionalOnMissingBean(AvpAuthorizationService.class)
    public AvpAuthorizationService permissiveAvpAuthorizationService() {
        return new PermissiveAvpAuthorizationService();
    }

    /**
     * Bean {@code avpSecurity} para o fallback permissivo, permitindo que
     * {@code @PreAuthorize("@avpSecurity.can(...)")} funcione tambem em dev/test.
     */
    @Bean(name = "avpSecurity")
    @Profile("!prod")
    @ConditionalOnMissingBean(name = "avpSecurity")
    public AvpMethodSecurity permissiveAvpMethodSecurity(AvpAuthorizationService avpService) {
        return new AvpMethodSecurity(avpService);
    }
}
