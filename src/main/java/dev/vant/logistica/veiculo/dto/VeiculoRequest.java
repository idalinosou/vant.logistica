package dev.vant.logistica.veiculo.dto;

import dev.vant.logistica.veiculo.StatusVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(
        @NotBlank(message = "A placa é obrigatória.")
        @Size(max = 12, message = "A placa deve ter no máximo 12 caracteres.")
        String placa,

        @NotBlank(message = "O modelo é obrigatório.")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres.")
        String modelo,

        @Size(max = 60, message = "O tipo deve ter no máximo 60 caracteres.")
        String tipo,

        @Size(max = 60, message = "A capacidade deve ter no máximo 60 caracteres.")
        String capacidade,

        StatusVeiculo status
) {
}
