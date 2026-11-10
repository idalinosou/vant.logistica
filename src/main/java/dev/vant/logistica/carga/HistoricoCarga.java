package dev.vant.logistica.carga;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "historico_carga")
public class HistoricoCarga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusCarga statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCarga novoStatus;

    @Column(nullable = false, updatable = false)
    private Instant dataAlteracao;

    protected HistoricoCarga() {
    }

    HistoricoCarga(Carga carga, StatusCarga statusAnterior, StatusCarga novoStatus) {
        this.carga = carga;
        this.statusAnterior = statusAnterior;
        this.novoStatus = novoStatus;
    }

    @PrePersist
    void aoCriar() {
        if (dataAlteracao == null) {
            dataAlteracao = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public StatusCarga getStatusAnterior() {
        return statusAnterior;
    }

    public StatusCarga getNovoStatus() {
        return novoStatus;
    }

    public Instant getDataAlteracao() {
        return dataAlteracao;
    }
}
