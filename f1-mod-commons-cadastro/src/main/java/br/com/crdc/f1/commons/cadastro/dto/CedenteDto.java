package br.com.crdc.f1.commons.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CedenteDto(
        UUID id,
        UUID pessoaId,
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String status,
        String kycVeredito,
        Instant habilitadoEm,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
