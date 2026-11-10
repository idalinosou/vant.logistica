package dev.vant.logistica.cliente.dto;

import dev.vant.logistica.cliente.Cliente;

import java.time.Instant;

public record ClienteResponse(Long id, String nome, String documento, String telefone, String email,
                              String cidade, Instant criadoEm) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getDocumento(),
                cliente.getTelefone(), cliente.getEmail(), cliente.getCidade(), cliente.getCriadoEm());
    }
}
