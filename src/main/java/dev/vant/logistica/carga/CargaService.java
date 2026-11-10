package dev.vant.logistica.carga;

import dev.vant.logistica.carga.dto.CargaRequest;
import dev.vant.logistica.carga.dto.CargaResponse;
import dev.vant.logistica.carga.dto.HistoricoResponse;
import dev.vant.logistica.cliente.Cliente;
import dev.vant.logistica.cliente.ClienteRepository;
import dev.vant.logistica.erro.RecursoNaoEncontradoException;
import dev.vant.logistica.erro.RegraDeNegocioException;
import dev.vant.logistica.motorista.Motorista;
import dev.vant.logistica.motorista.MotoristaRepository;
import dev.vant.logistica.veiculo.Veiculo;
import dev.vant.logistica.veiculo.VeiculoRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CargaService {

    private final CargaRepository cargas;
    private final ClienteRepository clientes;
    private final MotoristaRepository motoristas;
    private final VeiculoRepository veiculos;
    private final HistoricoCargaRepository historico;

    public CargaService(CargaRepository cargas, ClienteRepository clientes, MotoristaRepository motoristas,
                        VeiculoRepository veiculos, HistoricoCargaRepository historico) {
        this.cargas = cargas;
        this.clientes = clientes;
        this.motoristas = motoristas;
        this.veiculos = veiculos;
        this.historico = historico;
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> listar(StatusCarga status, String busca, int pagina, int tamanho) {
        Specification<Carga> filtro = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (busca != null && !busca.isBlank()) {
            String termo = "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
            filtro = filtro.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("codigo")), termo),
                    cb.like(cb.lower(root.get("origem")), termo),
                    cb.like(cb.lower(root.get("destino")), termo),
                    cb.like(cb.lower(root.join("cliente", JoinType.LEFT).get("nome")), termo)));
        }
        int paginaFinal = Math.max(pagina, 1);
        int tamanhoFinal = Math.min(Math.max(tamanho, 1), 50);
        Pageable paginacao = PageRequest.of(paginaFinal - 1, tamanhoFinal, Sort.by(Sort.Direction.DESC, "id"));
        LocalDate hoje = LocalDate.now();
        return cargas.findAll(filtro, paginacao).map(carga -> CargaResponse.de(carga, hoje));
    }

    @Transactional(readOnly = true)
    public CargaResponse buscar(Long id) {
        return CargaResponse.de(buscarEntidade(id), LocalDate.now());
    }

    @Transactional
    public CargaResponse criar(CargaRequest pedido) {
        String codigo = pedido.codigo().trim();
        if (cargas.existsByCodigoIgnoreCase(codigo)) {
            throw new RegraDeNegocioException("Já existe uma carga com o código \"" + codigo + "\".");
        }
        Carga carga = new Carga(
                codigo,
                pedido.origem().trim(),
                pedido.destino().trim(),
                cliente(pedido.clienteId()),
                motorista(pedido.motoristaId()),
                veiculo(pedido.veiculoId()),
                pedido.status(),
                pedido.previsaoEntrega());
        return CargaResponse.de(cargas.save(carga), LocalDate.now());
    }

    @Transactional
    public CargaResponse atualizar(Long id, CargaRequest pedido) {
        Carga carga = buscarEntidade(id);
        String codigo = pedido.codigo().trim();
        if (cargas.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new RegraDeNegocioException("Já existe outra carga com o código \"" + codigo + "\".");
        }
        validarTransicao(carga.getStatus(), pedido.status());
        carga.atualizar(
                codigo,
                pedido.origem().trim(),
                pedido.destino().trim(),
                cliente(pedido.clienteId()),
                motorista(pedido.motoristaId()),
                veiculo(pedido.veiculoId()),
                pedido.status(),
                pedido.previsaoEntrega());
        return CargaResponse.de(carga, LocalDate.now());
    }

    @Transactional
    public CargaResponse alterarStatus(Long id, StatusCarga novoStatus) {
        Carga carga = buscarEntidade(id);
        if (carga.getStatus() == novoStatus) {
            return CargaResponse.de(carga, LocalDate.now());
        }
        validarTransicao(carga.getStatus(), novoStatus);
        carga.alterarStatus(novoStatus);
        return CargaResponse.de(carga, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> historico(Long id) {
        buscarEntidade(id);
        return historico.findByCargaIdOrderByDataAlteracaoDesc(id).stream()
                .map(HistoricoResponse::de)
                .toList();
    }

    @Transactional
    public void excluir(Long id) {
        cargas.delete(buscarEntidade(id));
    }

    private void validarTransicao(StatusCarga atual, StatusCarga novo) {
        if (novo == null || novo == atual) {
            return;
        }
        if (!atual.podeMudarPara(novo)) {
            throw new RegraDeNegocioException("Não é possível alterar o status de \""
                    + atual.getRotulo() + "\" para \"" + novo.getRotulo() + "\".");
        }
    }

    private Carga buscarEntidade(Long id) {
        return cargas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carga " + id + " não encontrada."));
    }

    private Cliente cliente(Long id) {
        if (id == null) {
            return null;
        }
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado."));
    }

    private Motorista motorista(Long id) {
        if (id == null) {
            return null;
        }
        return motoristas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista " + id + " não encontrado."));
    }

    private Veiculo veiculo(Long id) {
        if (id == null) {
            return null;
        }
        return veiculos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo " + id + " não encontrado."));
    }
}
