package dev.vant.logistica.carga.dto;

import dev.vant.logistica.carga.StatusCarga;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CargaRequest(
        @NotBlank(message = "O código é obrigatório.")
        @Size(max = 40, message = "O código deve ter no máximo 40 caracteres.")
        String codigo,

        @NotBlank(message = "A origem é obrigatória.")
        @Size(max = 160, message = "A origem deve ter no máximo 160 caracteres.")
        String origem,

        @NotBlank(message = "O destino é obrigatório.")
        @Size(max = 160, message = "O destino deve ter no máximo 160 caracteres.")
        String destino,

        Long clienteId,
        Long motoristaId,
        Long veiculoId,
        StatusCarga status,
        LocalDate previsaoEntrega
) {
}
