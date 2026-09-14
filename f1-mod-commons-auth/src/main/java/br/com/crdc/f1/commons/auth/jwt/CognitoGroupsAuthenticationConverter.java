package br.com.crdc.f1.commons.auth.jwt;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Builder para o {@link JwtAuthenticationConverter} que mapeia o claim
 * {@code cognito:groups} do JWT para {@code GrantedAuthority}.
 *
 * <p>Defaults: claim {@code cognito:groups}, prefixo {@code ROLE_}. Assim, um JWT com
 * {@code cognito:groups: ["admins", "operators"]} resulta nas authorities
 * {@code ROLE_admins} e {@code ROLE_operators}, permitindo o uso de
 * {@code @PreAuthorize("hasRole('admins')")}.</p>
 *
 * <p>O builder permite customizar o nome do claim e o prefixo sem subclassing.</p>
 */
public class CognitoGroupsAuthenticationConverter {

    private String authoritiesClaimName = "cognito:groups";
    private String authorityPrefix = "ROLE_";

    /** Customiza o nome do claim que carrega os grupos (default {@code cognito:groups}). */
    public CognitoGroupsAuthenticationConverter claimName(String name) {
        this.authoritiesClaimName = name;
        return this;
    }

    /** Customiza o prefixo das authorities (default {@code ROLE_}). */
    public CognitoGroupsAuthenticationConverter prefix(String prefix) {
        this.authorityPrefix = prefix;
        return this;
    }

    /** Constroi o {@link JwtAuthenticationConverter} configurado. */
    public JwtAuthenticationConverter build() {
        JwtGrantedAuthoritiesConverter groupsConverter = new JwtGrantedAuthoritiesConverter();
        groupsConverter.setAuthoritiesClaimName(authoritiesClaimName);
        groupsConverter.setAuthorityPrefix(authorityPrefix);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(groupsConverter);
        return converter;
    }
}
