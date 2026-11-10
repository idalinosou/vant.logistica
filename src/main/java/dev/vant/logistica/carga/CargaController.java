package dev.vant.logistica.carga;

import dev.vant.logistica.carga.dto.CargaRequest;
import dev.vant.logistica.carga.dto.CargaResponse;
import dev.vant.logistica.carga.dto.HistoricoResponse;
import dev.vant.logistica.carga.dto.StatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cargas")
public class CargaController {

    private final CargaService servico;

    public CargaController(CargaService servico) {
        this.servico = servico;
    }

    @GetMapping
    public Page<CargaResponse> listar(
            @RequestParam(required = false) StatusCarga status,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return servico.listar(status, busca, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public CargaResponse buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CargaResponse> criar(@Valid @RequestBody CargaRequest pedido) {
        CargaResponse criada = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/api/cargas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public CargaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CargaRequest pedido) {
        return servico.atualizar(id, pedido);
    }

    @PatchMapping("/{id}/status")
    public CargaResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest pedido) {
        return servico.alterarStatus(id, pedido.status());
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoResponse> historico(@PathVariable Long id) {
        return servico.historico(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
