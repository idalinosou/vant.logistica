package dev.vant.logistica.veiculo;

public enum StatusVeiculo {
    DISPONIVEL("Disponível"),
    EM_OPERACAO("Em operação"),
    MANUTENCAO("Manutenção");

    private final String rotulo;

    StatusVeiculo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
