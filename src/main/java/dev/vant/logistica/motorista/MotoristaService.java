package dev.vant.logistica.motorista;

import dev.vant.logistica.carga.CargaRepository;
import dev.vant.logistica.erro.RecursoNaoEncontradoException;
import dev.vant.logistica.erro.RegraDeNegocioException;
import dev.vant.logistica.motorista.dto.MotoristaRequest;
import dev.vant.logistica.motorista.dto.MotoristaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

@Service
public class MotoristaService {

    private static final Collator ORDEM_PORTUGUES = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    private final MotoristaRepository repositorio;
    private final CargaRepository cargas;

    public MotoristaService(MotoristaRepository repositorio, CargaRepository cargas) {
        this.repositorio = repositorio;
        this.cargas = cargas;
    }

    @Transactional(readOnly = true)
    public List<MotoristaResponse> listar() {
        return repositorio.findAll().stream()
                .map(MotoristaResponse::de)
                .sorted((a, b) -> ORDEM_PORTUGUES.compare(a.nome(), b.nome()))
                .toList();
    }

    @Transactional
    public MotoristaResponse criar(MotoristaRequest pedido) {
        Motorista motorista = new Motorista(
                pedido.nome().trim(),
                texto(pedido.documento()),
                texto(pedido.telefone()),
                texto(pedido.cnh()),
                pedido.status());
        return MotoristaResponse.de(repositorio.save(motorista));
    }

    @Transactional
    public MotoristaResponse atualizar(Long id, MotoristaRequest pedido) {
        Motorista motorista = buscarEntidade(id);
        motorista.atualizar(
                pedido.nome().trim(),
                texto(pedido.documento()),
                texto(pedido.telefone()),
                texto(pedido.cnh()),
                pedido.status());
        return MotoristaResponse.de(motorista);
    }

    @Transactional
    public void excluir(Long id) {
        Motorista motorista = buscarEntidade(id);
        if (cargas.existsByMotoristaId(id)) {
            throw new RegraDeNegocioException("O motorista \"" + motorista.getNome() + "\" está vinculado a cargas e não pode ser excluído.");
        }
        repositorio.delete(motorista);
    }

    private Motorista buscarEntidade(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista " + id + " não encontrado."));
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
