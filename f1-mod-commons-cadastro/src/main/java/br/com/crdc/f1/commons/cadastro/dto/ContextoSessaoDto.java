package br.com.crdc.f1.commons.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContextoSessaoDto(
        UUID usuarioId,
        String nomeExibicao,
        String email,
        List<Vinculo> vinculos,
        List<String> plataformasHabilitadas
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vinculo(
            UUID id,
            UUID pessoaId,
            UUID personaId,
            String perfil,
            String plataforma,
            Boolean ativo,
            String razaoSocial
    ) {
    }
}
