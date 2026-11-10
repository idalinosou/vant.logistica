package dev.vant.logistica.painel;

import dev.vant.logistica.carga.CargaRepository;
import dev.vant.logistica.carga.StatusCarga;
import dev.vant.logistica.cliente.ClienteRepository;
import dev.vant.logistica.motorista.MotoristaRepository;
import dev.vant.logistica.motorista.StatusMotorista;
import dev.vant.logistica.painel.dto.PainelResponse;
import dev.vant.logistica.veiculo.StatusVeiculo;
import dev.vant.logistica.veiculo.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;

@Service
public class PainelService {

    private final CargaRepository cargas;
    private final VeiculoRepository veiculos;
    private final MotoristaRepository motoristas;
    private final ClienteRepository clientes;

    public PainelService(CargaRepository cargas, VeiculoRepository veiculos,
                         MotoristaRepository motoristas, ClienteRepository clientes) {
        this.cargas = cargas;
        this.veiculos = veiculos;
        this.motoristas = motoristas;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public PainelResponse resumo() {
        return new PainelResponse(
                cargas.count(),
                cargas.countByStatus(StatusCarga.PENDENTE),
                cargas.countByStatus(StatusCarga.EM_TRANSITO),
                cargas.countByStatus(StatusCarga.ENTREGUE),
                cargas.countByStatus(StatusCarga.CANCELADO),
                cargas.contarAtrasadas(LocalDate.now(), EnumSet.of(StatusCarga.PENDENTE, StatusCarga.EM_TRANSITO)),
                veiculos.count(),
                veiculos.countByStatus(StatusVeiculo.DISPONIVEL),
                motoristas.count(),
                motoristas.countByStatus(StatusMotorista.ATIVO),
                clientes.count());
    }
}
