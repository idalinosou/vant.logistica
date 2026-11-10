package dev.vant.logistica.painel;

import dev.vant.logistica.BaseIntegracao;
import dev.vant.logistica.carga.StatusCarga;
import dev.vant.logistica.motorista.Motorista;
import dev.vant.logistica.motorista.StatusMotorista;
import dev.vant.logistica.veiculo.StatusVeiculo;
import dev.vant.logistica.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PainelIntegrationTest extends BaseIntegracao {

    @Test
    void resumeCargasPorStatusERecursos() throws Exception {
        cliente("Cliente A");
        motorista("Motorista A");

        Veiculo disponivel = veiculos.save(new Veiculo("AAA1A11", "Van", "Van",
                "1 tonelada", StatusVeiculo.DISPONIVEL));
        veiculos.save(new Veiculo("BBB2B22", "Caminhão", "Caminhão",
                "10 toneladas", StatusVeiculo.MANUTENCAO));
        motoristas.save(new Motorista("Inativo", null, null, null, StatusMotorista.INATIVO));

        carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));
        carga("CAR-002", StatusCarga.PENDENTE, LocalDate.now().plusDays(2));
        carga("CAR-003", StatusCarga.EM_TRANSITO, LocalDate.now().plusDays(3));
        carga("CAR-004", StatusCarga.ENTREGUE, LocalDate.now().minusDays(3));
        carga("CAR-005", StatusCarga.CANCELADO, LocalDate.now().minusDays(1));
        cargaComVinculos("CAR-006", null, null, disponivel);

        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCargas").value(6))
                .andExpect(jsonPath("$.pendentes").value(3))
                .andExpect(jsonPath("$.emTransito").value(1))
                .andExpect(jsonPath("$.entregues").value(1))
                .andExpect(jsonPath("$.canceladas").value(1))
                .andExpect(jsonPath("$.veiculos").value(2))
                .andExpect(jsonPath("$.veiculosDisponiveis").value(1))
                .andExpect(jsonPath("$.motoristas").value(2))
                .andExpect(jsonPath("$.motoristasAtivos").value(1))
                .andExpect(jsonPath("$.clientes").value(1));
    }

    @Test
    void contaSomenteCargasAtrasadasEmAberto() throws Exception {
        carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().minusDays(2));
        carga("CAR-002", StatusCarga.EM_TRANSITO, LocalDate.now().minusDays(1));
        carga("CAR-003", StatusCarga.ENTREGUE, LocalDate.now().minusDays(2));
        carga("CAR-004", StatusCarga.CANCELADO, LocalDate.now().minusDays(2));
        carga("CAR-005", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));

        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasadas").value(2));
    }

    @Test
    void painelVazioRetornaZeros() throws Exception {
        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCargas").value(0))
                .andExpect(jsonPath("$.clientes").value(0));
    }
}
