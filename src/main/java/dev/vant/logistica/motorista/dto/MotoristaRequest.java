package dev.vant.logistica.motorista.dto;

import dev.vant.logistica.motorista.StatusMotorista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoristaRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
        String nome,

        @Size(max = 30, message = "O documento deve ter no máximo 30 caracteres.")
        String documento,

        @Size(max = 40, message = "O telefone deve ter no máximo 40 caracteres.")
        String telefone,

        @Size(max = 40, message = "A CNH deve ter no máximo 40 caracteres.")
        String cnh,

        StatusMotorista status
) {
}
