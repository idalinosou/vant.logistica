package dev.vant.logistica.veiculo;

import dev.vant.logistica.carga.CargaRepository;
import dev.vant.logistica.erro.RecursoNaoEncontradoException;
import dev.vant.logistica.erro.RegraDeNegocioException;
import dev.vant.logistica.veiculo.dto.VeiculoRequest;
import dev.vant.logistica.veiculo.dto.VeiculoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository repositorio;
    private final CargaRepository cargas;

    public VeiculoService(VeiculoRepository repositorio, CargaRepository cargas) {
        this.repositorio = repositorio;
        this.cargas = cargas;
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar() {
        return repositorio.findAll().stream()
                .map(VeiculoResponse::de)
                .sorted(Comparator.comparing(VeiculoResponse::placa))
                .toList();
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest pedido) {
        String placa = normalizarPlaca(pedido.placa());
        if (repositorio.existsByPlacaIgnoreCase(placa)) {
            throw new RegraDeNegocioException("Já existe um veículo com a placa " + placa + ".");
        }
        Veiculo veiculo = new Veiculo(placa, pedido.modelo().trim(), texto(pedido.tipo()),
                texto(pedido.capacidade()), pedido.status());
        return VeiculoResponse.de(repositorio.save(veiculo));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest pedido) {
        Veiculo veiculo = buscarEntidade(id);
        String placa = normalizarPlaca(pedido.placa());
        if (repositorio.existsByPlacaIgnoreCaseAndIdNot(placa, id)) {
            throw new RegraDeNegocioException("Já existe um veículo com a placa " + placa + ".");
        }
        veiculo.atualizar(placa, pedido.modelo().trim(), texto(pedido.tipo()), texto(pedido.capacidade()), pedido.status());
        return VeiculoResponse.de(veiculo);
    }

    @Transactional
    public void excluir(Long id) {
        Veiculo veiculo = buscarEntidade(id);
        if (cargas.existsByVeiculoId(id)) {
            throw new RegraDeNegocioException("O veículo " + veiculo.getPlaca() + " está vinculado a cargas e não pode ser excluído.");
        }
        repositorio.delete(veiculo);
    }

    private Veiculo buscarEntidade(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo " + id + " não encontrado."));
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase();
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
