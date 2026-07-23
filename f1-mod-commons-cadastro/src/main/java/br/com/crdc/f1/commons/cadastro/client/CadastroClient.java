package br.com.crdc.f1.commons.cadastro.client;

import br.com.crdc.f1.commons.cadastro.dto.CedenteDto;
import br.com.crdc.f1.commons.cadastro.dto.ContextoSessaoDto;
import br.com.crdc.f1.commons.cadastro.dto.HabilitacaoDto;
import br.com.crdc.f1.commons.cadastro.dto.PersonaDto;
import br.com.crdc.f1.commons.cadastro.dto.PessoaDto;
import br.com.crdc.f1.commons.cadastro.exception.CadastroClientException;
import br.com.crdc.f1.commons.cadastro.exception.CadastroNotFoundException;
import br.com.crdc.f1.commons.cadastro.util.DocumentoMasker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Shared HTTP client for the Cadastro Centralizado service.
 *
 * <p>This is the SINGLE entry point for every F1-Mod service that needs identity,
 * autorização, cedente ou KYC data from the Cadastro (ADR-030 §"Fronteira F1-Mod ↔
 * Cadastro").</p>
 */
public class CadastroClient {

    private static final Logger log = LoggerFactory.getLogger(CadastroClient.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public CadastroClient(RestClient restClient, CircuitBreaker circuitBreaker, Retry retry) {
        this.restClient = Objects.requireNonNull(restClient, "restClient");
        this.circuitBreaker = Objects.requireNonNull(circuitBreaker, "circuitBreaker");
        this.retry = Objects.requireNonNull(retry, "retry");
    }

    public PessoaDto getPessoa(UUID id) {
        Objects.requireNonNull(id, "id");
        return callResilient("getPessoa", () ->
                restClient.get()
                        .uri("/pessoas/{id}", id)
                        .retrieve()
                        .body(PessoaDto.class));
    }

    public List<PersonaDto> getPersonas(UUID pessoaId) {
        Objects.requireNonNull(pessoaId, "pessoaId");
        List<PersonaDto> body = callResilient("getPersonas", () ->
                restClient.get()
                        .uri("/pessoas/{id}/personas", pessoaId)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<PersonaDto>>() {}));
        return body == null ? List.of() : body;
    }

    public HabilitacaoDto getHabilitacao(UUID personaId, String plataforma) {
        Objects.requireNonNull(personaId, "personaId");
        Objects.requireNonNull(plataforma, "plataforma");
        return callResilient("getHabilitacao", () ->
                restClient.get()
                        .uri("/habilitacoes/{personaId}/{plataforma}", personaId, plataforma)
                        .retrieve()
                        .body(HabilitacaoDto.class));
    }

    public CedenteDto getCedentePorCnpj(String cnpj) {
        Objects.requireNonNull(cnpj, "cnpj");
        String digits = cnpj.replaceAll("\\D", "");
        String masked = DocumentoMasker.mask(digits);

        CedentesResponse response = callResilient("getCedentePorCnpj", () ->
                restClient.get()
                        .uri(uri -> uri.path("/api/v1/cedentes").queryParam("cnpj", digits).build())
                        .retrieve()
                        .body(CedentesResponse.class));

        if (response == null || response.items() == null || response.items().isEmpty()) {
            log.info("Cadastro: cedente not found for cnpj={}", masked);
            throw new CadastroNotFoundException(
                    "cedente not found for cnpj=" + masked);
        }
        return response.items().get(0);
    }

    public ContextoSessaoDto getContextoSessao() {
        return callResilient("getContextoSessao", () ->
                restClient.get()
                        .uri("/me/contexto")
                        .retrieve()
                        .body(ContextoSessaoDto.class));
    }

    private <T> T callResilient(String opName, Supplier<T> op) {
        Supplier<T> translated = () -> {
            try {
                return op.get();
            } catch (HttpClientErrorException.NotFound nf) {
                log.info("Cadastro {}: 404 not found (semantic — not a breaker failure)", opName);
                throw new CadastroNotFoundException(opName + ": not found");
            }
        };

        Supplier<T> cbDecorated = CircuitBreaker.decorateSupplier(circuitBreaker, translated);
        Supplier<T> decorated = Retry.decorateSupplier(retry, cbDecorated);
        try {
            return decorated.get();
        } catch (CadastroNotFoundException nf) {
            throw nf;
        } catch (CallNotPermittedException cbOpen) {
            log.warn("Cadastro {}: circuit breaker OPEN — request rejected", opName);
            throw new CadastroClientException(
                    opName + ": circuit breaker open (Cadastro Centralizado is degraded)", cbOpen);
        } catch (Exception ex) {
            log.error("Cadastro {}: call failed after retries — {}",
                    opName, ex.getClass().getSimpleName());
            throw new CadastroClientException(
                    opName + ": call failed after retries — " + ex.getClass().getSimpleName(), ex);
        }
    }

    record CedentesResponse(List<CedenteDto> items) {
    }
}
