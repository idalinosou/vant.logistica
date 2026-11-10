package dev.vant.logistica.carga.dto;

import dev.vant.logistica.carga.StatusCarga;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "O status é obrigatório.") StatusCarga status) {
}
