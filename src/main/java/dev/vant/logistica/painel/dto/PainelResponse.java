package dev.vant.logistica.painel.dto;

public record PainelResponse(
        long totalCargas,
        long pendentes,
        long emTransito,
        long entregues,
        long canceladas,
        long atrasadas,
        long veiculos,
        long veiculosDisponiveis,
        long motoristas,
        long motoristasAtivos,
        long clientes
) {
}
