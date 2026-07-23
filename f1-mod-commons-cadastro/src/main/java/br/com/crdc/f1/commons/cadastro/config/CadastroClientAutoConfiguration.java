package br.com.crdc.f1.commons.cadastro.config;

import br.com.crdc.f1.commons.cadastro.client.CadastroClient;
import br.com.crdc.f1.commons.cadastro.client.JwtPropagationInterceptor;
import br.com.crdc.f1.commons.cadastro.exception.CadastroNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@AutoConfiguration
@ConditionalOnProperty(prefix = "crdc.cadastro", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CadastroClientProperties.class)
public class CadastroClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CadastroClientAutoConfiguration.class);

    static final String CB_NAME = "cadastroClient";
    static final String RETRY_NAME = "cadastroClient";

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry cadastroCircuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry cadastroRetryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean(name = "cadastroCircuitBreaker")
    public CircuitBreaker cadastroCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .ignoreExceptions(CadastroNotFoundException.class)
                .build();
        return registry.circuitBreaker(CB_NAME, config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cadastroRetry")
    public Retry cadastroRetry(RetryRegistry registry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .ignoreExceptions(CadastroNotFoundException.class)
                .build();
        return registry.retry(RETRY_NAME, config);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtPropagationInterceptor jwtPropagationInterceptor() {
        return new JwtPropagationInterceptor();
    }

    @Bean(name = "cadastroRestClient")
    @ConditionalOnMissingBean(name = "cadastroRestClient")
    public RestClient cadastroRestClient(CadastroClientProperties props,
                                         JwtPropagationInterceptor interceptor) {
        String baseUrl = props.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "crdc.cadastro.base-url is required when crdc.cadastro.enabled=true");
        }

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        int connectTimeoutMs = (int) Math.min(props.getConnectTimeout().toMillis(), Integer.MAX_VALUE);
        int readTimeoutMs = (int) Math.min(props.getReadTimeout().toMillis(), Integer.MAX_VALUE);
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);

        log.info("Cadastro client wiring: base-url={}, connect-timeout={}, read-timeout={}",
                baseUrl, props.getConnectTimeout(), props.getReadTimeout());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(rf)
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CadastroClient cadastroClient(
            @Qualifier("cadastroRestClient") RestClient cadastroRestClient,
            CircuitBreaker cadastroCircuitBreaker,
            Retry cadastroRetry) {
        return new CadastroClient(cadastroRestClient, cadastroCircuitBreaker, cadastroRetry);
    }
}
