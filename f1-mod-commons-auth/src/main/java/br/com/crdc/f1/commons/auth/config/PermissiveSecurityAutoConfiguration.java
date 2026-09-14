package br.com.crdc.f1.commons.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Fallback permissivo ({@code permitAll}) para ambientes de desenvolvimento/teste.
 *
 * <p>Ativa quando {@code crdc.security.enabled} e {@code false}/ausente E o profile
 * ativo NAO e {@code prod}. O {@code @Profile("!prod")} garante que este bean nunca
 * carrega em producao, mesmo com a propriedade incorretamente desligada — em prod
 * sem JWT, a aplicacao fica sem {@code SecurityFilterChain} e o
 * {@link AuthProdSafetyGuard} interrompe o startup.</p>
 */
@Configuration
@Profile("!prod")
@ConditionalOnProperty(name = "crdc.security.enabled", havingValue = "false", matchIfMissing = true)
@EnableWebSecurity
public class PermissiveSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain permissiveFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
