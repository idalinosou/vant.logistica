package dev.vant.logistica.carga.dto;

import dev.vant.logistica.carga.Carga;

import java.time.Instant;
import java.time.LocalDate;

public record CargaResponse(
        Long id,
        String codigo,
        String origem,
        String destino,
        Long clienteId,
        String clienteNome,
        Long motoristaId,
        String motoristaNome,
        Long veiculoId,
        String veiculoPlaca,
        String veiculoModelo,
        String status,
        String statusRotulo,
        LocalDate previsaoEntrega,
        Instant criadaEm,
        boolean atrasada
) {

    public static CargaResponse de(Carga carga, LocalDate hoje) {
        return new CargaResponse(
                carga.getId(),
                carga.getCodigo(),
                carga.getOrigem(),
                carga.getDestino(),
                carga.getCliente() == null ? null : carga.getCliente().getId(),
                carga.getCliente() == null ? null : carga.getCliente().getNome(),
                carga.getMotorista() == null ? null : carga.getMotorista().getId(),
                carga.getMotorista() == null ? null : carga.getMotorista().getNome(),
                carga.getVeiculo() == null ? null : carga.getVeiculo().getId(),
                carga.getVeiculo() == null ? null : carga.getVeiculo().getPlaca(),
                carga.getVeiculo() == null ? null : carga.getVeiculo().getModelo(),
                carga.getStatus().name(),
                carga.getStatus().getRotulo(),
                carga.getPrevisaoEntrega(),
                carga.getCriadaEm(),
                carga.atrasada(hoje));
    }
}
