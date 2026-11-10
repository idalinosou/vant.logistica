package dev.vant.logistica.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
        String nome,

        @Size(max = 30, message = "O documento deve ter no máximo 30 caracteres.")
        String documento,

        @Size(max = 40, message = "O telefone deve ter no máximo 40 caracteres.")
        String telefone,

        @Email(message = "E-mail inválido.")
        @Size(max = 140, message = "O e-mail deve ter no máximo 140 caracteres.")
        String email,

        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
        String cidade
) {
}
