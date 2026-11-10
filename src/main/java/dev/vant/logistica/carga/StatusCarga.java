package dev.vant.logistica.carga;

import java.util.EnumSet;

public enum StatusCarga {
    PENDENTE("Pendente"),
    EM_TRANSITO("Em trânsito"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String rotulo;

    StatusCarga(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean podeMudarPara(StatusCarga destino) {
        return switch (this) {
            case PENDENTE -> EnumSet.of(EM_TRANSITO, CANCELADO).contains(destino);
            case EM_TRANSITO -> EnumSet.of(ENTREGUE, CANCELADO).contains(destino);
            case ENTREGUE, CANCELADO -> false;
        };
    }

    public boolean finalizado() {
        return this == ENTREGUE || this == CANCELADO;
    }
}
