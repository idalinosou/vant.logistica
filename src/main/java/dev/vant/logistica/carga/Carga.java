package dev.vant.logistica.carga;

import dev.vant.logistica.cliente.Cliente;
import dev.vant.logistica.motorista.Motorista;
import dev.vant.logistica.veiculo.Veiculo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cargas")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String origem;

    @Column(nullable = false, length = 160)
    private String destino;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCarga status = StatusCarga.PENDENTE;

    private LocalDate previsaoEntrega;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @OneToMany(mappedBy = "carga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoCarga> historico = new ArrayList<>();

    protected Carga() {
    }

    public Carga(String codigo, String origem, String destino, Cliente cliente, Motorista motorista,
                 Veiculo veiculo, StatusCarga status, LocalDate previsaoEntrega) {
        this.codigo = codigo;
        this.origem = origem;
        this.destino = destino;
        this.cliente = cliente;
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.status = status == null ? StatusCarga.PENDENTE : status;
        this.previsaoEntrega = previsaoEntrega;
        registrarHistorico(null, this.status);
    }

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }

    public void atualizar(String codigo, String origem, String destino, Cliente cliente, Motorista motorista,
                          Veiculo veiculo, StatusCarga novoStatus, LocalDate previsaoEntrega) {
        this.codigo = codigo;
        this.origem = origem;
        this.destino = destino;
        this.cliente = cliente;
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.previsaoEntrega = previsaoEntrega;
        if (novoStatus != null && novoStatus != this.status) {
            StatusCarga anterior = this.status;
            this.status = novoStatus;
            registrarHistorico(anterior, novoStatus);
        }
    }

    public void alterarStatus(StatusCarga novoStatus) {
        StatusCarga anterior = this.status;
        this.status = novoStatus;
        registrarHistorico(anterior, novoStatus);
    }

    private void registrarHistorico(StatusCarga anterior, StatusCarga novo) {
        historico.add(new HistoricoCarga(this, anterior, novo));
    }

    public boolean atrasada(LocalDate hoje) {
        return previsaoEntrega != null && previsaoEntrega.isBefore(hoje) && !status.finalizado();
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public StatusCarga getStatus() {
        return status;
    }

    public LocalDate getPrevisaoEntrega() {
        return previsaoEntrega;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public List<HistoricoCarga> getHistorico() {
        return historico;
    }
}
