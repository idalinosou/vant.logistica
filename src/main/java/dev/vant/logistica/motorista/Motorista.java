package dev.vant.logistica.motorista;

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
@Table(name = "motoristas")
public class Motorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String nome;

    @Column(length = 30)
    private String documento;

    @Column(length = 40)
    private String telefone;

    @Column(length = 40)
    private String cnh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMotorista status = StatusMotorista.ATIVO;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Motorista() {
    }

    public Motorista(String nome, String documento, String telefone, String cnh, StatusMotorista status) {
        this.nome = nome;
        this.documento = documento;
        this.telefone = telefone;
        this.cnh = cnh;
        this.status = status == null ? StatusMotorista.ATIVO : status;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public void atualizar(String nome, String documento, String telefone, String cnh, StatusMotorista status) {
        this.nome = nome;
        this.documento = documento;
        this.telefone = telefone;
        this.cnh = cnh;
        this.status = status == null ? StatusMotorista.ATIVO : status;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDocumento() {
        return documento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCnh() {
        return cnh;
    }

    public StatusMotorista getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
