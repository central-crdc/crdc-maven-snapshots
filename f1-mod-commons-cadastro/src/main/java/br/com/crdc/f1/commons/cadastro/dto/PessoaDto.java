package br.com.crdc.f1.commons.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PessoaDto(
        UUID id,
        Long idLegado,
        String tipoPessoa,
        String subtipoPessoa,
        String razaoSocial,
        String nomeFantasia,
        String cpfCnpj,
        String email,
        String telefone,
        Boolean ativo,
        Instant createdAt,
        Instant updatedAt
) {
}
