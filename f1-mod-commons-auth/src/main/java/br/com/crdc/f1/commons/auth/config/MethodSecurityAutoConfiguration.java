package br.com.crdc.f1.commons.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Habilita o method security ({@code @PreAuthorize}/{@code @PostAuthorize}) de forma
 * INDEPENDENTE da cadeia de filtros HTTP.
 *
 * <p>Method security e a cadeia de filtros (autenticacao) sao ortogonais no Spring
 * Security. Separar o {@code @EnableMethodSecurity} da {@link JwtSecurityAutoConfiguration}
 * permite dois cenarios legitimos:</p>
 * <ul>
 *   <li><b>Auth no gateway:</b> o servico roda com cadeia permissiva (autenticacao
 *       feita upstream), mas ainda quer enforcar {@code @PreAuthorize} localmente com
 *       base nas roles do token propagado.</li>
 *   <li><b>Auth local:</b> cadeia JWT ativa ({@code crdc.security.enabled=true}) — o
 *       method security continua valendo, sem duplicidade (Spring de-duplica o
 *       {@code @EnableMethodSecurity}).</li>
 * </ul>
 *
 * <p>Sempre ativa: declarar {@code @EnableMethodSecurity} sem nenhum
 * {@code @PreAuthorize} no codigo e inofensivo (nenhum ponto de corte casa).</p>
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityAutoConfiguration {
}
