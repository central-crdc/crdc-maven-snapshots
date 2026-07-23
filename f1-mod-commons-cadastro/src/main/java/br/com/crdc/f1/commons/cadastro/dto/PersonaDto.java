package br.com.crdc.f1.commons.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PersonaDto(
        UUID id,
        UUID pessoaId,
        String tipoPersona,
        Boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
