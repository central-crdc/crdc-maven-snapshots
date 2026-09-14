package br.com.crdc.f1.commons.auth.config;

import br.com.crdc.f1.commons.auth.avp.AvpAuthorizationService;
import br.com.crdc.f1.commons.auth.avp.AvpAuthorizationServiceImpl;
import br.com.crdc.f1.commons.auth.avp.AvpMethodSecurity;
import br.com.crdc.f1.commons.auth.avp.CedarPrincipalResolver;
import br.com.crdc.f1.commons.auth.avp.CognitoGroupsPrincipalResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.verifiedpermissions.VerifiedPermissionsClient;

/**
 * Auto-configuration da autorizacao AVP/Cedar real.
 *
 * <p>Ativa quando {@code crdc.avp.enabled=true} E o AVP SDK esta no classpath
 * ({@code @ConditionalOnClass}). Sem o SDK, a config e ignorada graciosamente (o
 * fallback permissivo entra pela {@code AuthAutoConfiguration} fora do profile prod).</p>
 *
 * <p>Registra: {@code VerifiedPermissionsClient}, {@code CedarPrincipalResolver} default,
 * {@code AvpAuthorizationServiceImpl} e {@code AvpMethodSecurity} — todos com
 * {@code @ConditionalOnMissingBean} para permitir override pelos consumidores.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "crdc.avp", name = "enabled", havingValue = "true")
@ConditionalOnClass(VerifiedPermissionsClient.class)
public class AvpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VerifiedPermissionsClient.class)
    public VerifiedPermissionsClient verifiedPermissionsClient() {
        String region = System.getenv().getOrDefault("AWS_REGION", "sa-east-1");
        return VerifiedPermissionsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(CedarPrincipalResolver.class)
    public CedarPrincipalResolver cedarPrincipalResolver(AvpProperties avpProps) {
        return new CognitoGroupsPrincipalResolver(avpProps);
    }

    @Bean
    @ConditionalOnMissingBean(AvpAuthorizationService.class)
    public AvpAuthorizationServiceImpl avpAuthorizationService(
            VerifiedPermissionsClient avpClient,
            AvpProperties avpProps) {
        return new AvpAuthorizationServiceImpl(avpClient, avpProps);
    }

    @Bean(name = "avpSecurity")
    @ConditionalOnMissingBean(name = "avpSecurity")
    public AvpMethodSecurity avpMethodSecurity(AvpAuthorizationService avpService) {
        return new AvpMethodSecurity(avpService);
    }
}
