package dev.vant.logistica.veiculo.dto;

import dev.vant.logistica.veiculo.Veiculo;

import java.time.Instant;

public record VeiculoResponse(Long id, String placa, String modelo, String tipo, String capacidade,
                              String status, String statusRotulo, Instant criadoEm) {

    public static VeiculoResponse de(Veiculo veiculo) {
        return new VeiculoResponse(veiculo.getId(), veiculo.getPlaca(), veiculo.getModelo(),
                veiculo.getTipo(), veiculo.getCapacidade(), veiculo.getStatus().name(),
                veiculo.getStatus().getRotulo(), veiculo.getCriadoEm());
    }
}
