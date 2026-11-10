package dev.vant.logistica.carga.dto;

import dev.vant.logistica.carga.HistoricoCarga;

import java.time.Instant;

public record HistoricoResponse(Long id, String statusAnterior, String statusAnteriorRotulo,
                                String novoStatus, String novoStatusRotulo, Instant dataAlteracao) {

    public static HistoricoResponse de(HistoricoCarga historico) {
        return new HistoricoResponse(
                historico.getId(),
                historico.getStatusAnterior() == null ? null : historico.getStatusAnterior().name(),
                historico.getStatusAnterior() == null ? null : historico.getStatusAnterior().getRotulo(),
                historico.getNovoStatus().name(),
                historico.getNovoStatus().getRotulo(),
                historico.getDataAlteracao());
    }
}
