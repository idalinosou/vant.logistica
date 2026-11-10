package dev.vant.logistica.veiculo;

import dev.vant.logistica.BaseIntegracao;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeiculoIntegrationTest extends BaseIntegracao {

    @Test
    void criaVeiculoNormalizandoPlaca() throws Exception {
        mvc.perform(post("/api/veiculos").contentType(APPLICATION_JSON)
                        .content("""
                                {"placa":"abc1d23","modelo":"Caminhão Baú 01","tipo":"Caminhão",
                                 "capacidade":"12 toneladas","status":"DISPONIVEL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.statusRotulo").value("Disponível"));
    }

    @Test
    void rejeitaPlacaDuplicada() throws Exception {
        veiculo("ABC1D23");

        mvc.perform(post("/api/veiculos").contentType(APPLICATION_JSON)
                        .content("""
                                {"placa":"abc1d23","modelo":"Outro caminhão"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Já existe um veículo")));
    }

    @Test
    void rejeitaEdicaoComPlacaDeOutroVeiculo() throws Exception {
        Veiculo primeiro = veiculo("ABC1D23");
        veiculo("XYZ9K88");

        mvc.perform(put("/api/veiculos/" + primeiro.getId()).contentType(APPLICATION_JSON)
                        .content("""
                                {"placa":"XYZ9K88","modelo":"Caminhão Baú 01"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void impedeExcluirVeiculoVinculadoACarga() throws Exception {
        Veiculo veiculo = veiculo("ABC1D23");
        cargaComVinculos("CAR-001", null, null, veiculo);

        mvc.perform(delete("/api/veiculos/" + veiculo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("vinculado a cargas")));

        assertEquals(1, veiculos.count());
    }

    @Test
    void excluiVeiculoSemVinculo() throws Exception {
        Veiculo veiculo = veiculo("ABC1D23");

        mvc.perform(delete("/api/veiculos/" + veiculo.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, veiculos.count());
    }
}
