package dev.vant.logistica.motorista.dto;

import dev.vant.logistica.motorista.Motorista;

import java.time.Instant;

public record MotoristaResponse(Long id, String nome, String documento, String telefone, String cnh,
                                String status, String statusRotulo, Instant criadoEm) {

    public static MotoristaResponse de(Motorista motorista) {
        return new MotoristaResponse(motorista.getId(), motorista.getNome(), motorista.getDocumento(),
                motorista.getTelefone(), motorista.getCnh(), motorista.getStatus().name(),
                motorista.getStatus().getRotulo(), motorista.getCriadoEm());
    }
}
