package br.com.crdc.f1.commons.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HabilitacaoDto(
        UUID personaId,
        String plataforma,
        String status,
        String motivo,
        Instant habilitadaEm,
        Instant revogadaEm,
        Boolean kycValido,
        Instant kycExpiraEm
) {
    public boolean isAtiva() {
        return "ATIVA".equals(status) && Boolean.TRUE.equals(kycValido);
    }
}
