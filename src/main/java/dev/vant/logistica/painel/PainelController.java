package dev.vant.logistica.painel;

import dev.vant.logistica.painel.dto.PainelResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService servico;

    public PainelController(PainelService servico) {
        this.servico = servico;
    }

    @GetMapping
    public PainelResponse resumo() {
        return servico.resumo();
    }
}
