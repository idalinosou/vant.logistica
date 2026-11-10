package dev.vant.logistica.motorista;

import dev.vant.logistica.motorista.dto.MotoristaRequest;
import dev.vant.logistica.motorista.dto.MotoristaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private final MotoristaService servico;

    public MotoristaController(MotoristaService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<MotoristaResponse> listar() {
        return servico.listar();
    }

    @PostMapping
    public ResponseEntity<MotoristaResponse> criar(@Valid @RequestBody MotoristaRequest pedido) {
        MotoristaResponse criado = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/api/motoristas/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public MotoristaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MotoristaRequest pedido) {
        return servico.atualizar(id, pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
