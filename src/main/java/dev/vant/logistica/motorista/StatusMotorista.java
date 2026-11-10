package dev.vant.logistica.motorista;

public enum StatusMotorista {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String rotulo;

    StatusMotorista(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
