package dev.vant.logistica.veiculo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String placa;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(length = 60)
    private String tipo;

    @Column(length = 60)
    private String capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Veiculo() {
    }

    public Veiculo(String placa, String modelo, String tipo, String capacidade, StatusVeiculo status) {
        this.placa = placa;
        this.modelo = modelo;
        this.tipo = tipo;
        this.capacidade = capacidade;
        this.status = status == null ? StatusVeiculo.DISPONIVEL : status;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public void atualizar(String placa, String modelo, String tipo, String capacidade, StatusVeiculo status) {
        this.placa = placa;
        this.modelo = modelo;
        this.tipo = tipo;
        this.capacidade = capacidade;
        this.status = status == null ? StatusVeiculo.DISPONIVEL : status;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCapacidade() {
        return capacidade;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
