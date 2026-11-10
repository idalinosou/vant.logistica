package dev.vant.logistica.cliente;

import dev.vant.logistica.carga.CargaRepository;
import dev.vant.logistica.cliente.dto.ClienteRequest;
import dev.vant.logistica.cliente.dto.ClienteResponse;
import dev.vant.logistica.erro.RecursoNaoEncontradoException;
import dev.vant.logistica.erro.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

@Service
public class ClienteService {

    private static final Collator ORDEM_PORTUGUES = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    private final ClienteRepository repositorio;
    private final CargaRepository cargas;

    public ClienteService(ClienteRepository repositorio, CargaRepository cargas) {
        this.repositorio = repositorio;
        this.cargas = cargas;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repositorio.findAll().stream()
                .map(ClienteResponse::de)
                .sorted((a, b) -> ORDEM_PORTUGUES.compare(a.nome(), b.nome()))
                .toList();
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest pedido) {
        Cliente cliente = new Cliente(
                pedido.nome().trim(),
                texto(pedido.documento()),
                texto(pedido.telefone()),
                texto(pedido.email()),
                texto(pedido.cidade()));
        return ClienteResponse.de(repositorio.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest pedido) {
        Cliente cliente = buscarEntidade(id);
        cliente.atualizar(
                pedido.nome().trim(),
                texto(pedido.documento()),
                texto(pedido.telefone()),
                texto(pedido.email()),
                texto(pedido.cidade()));
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        if (cargas.existsByClienteId(id)) {
            throw new RegraDeNegocioException("O cliente \"" + cliente.getNome() + "\" está vinculado a cargas e não pode ser excluído.");
        }
        repositorio.delete(cliente);
    }

    private Cliente buscarEntidade(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado."));
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
