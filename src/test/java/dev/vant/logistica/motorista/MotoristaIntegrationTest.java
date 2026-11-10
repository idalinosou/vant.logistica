package dev.vant.logistica.motorista;

import dev.vant.logistica.BaseIntegracao;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MotoristaIntegrationTest extends BaseIntegracao {

    @Test
    void criaMotoristaComStatusPadraoAtivo() throws Exception {
        mvc.perform(post("/api/motoristas").contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"João Silva","documento":"123.456.789-00",
                                 "telefone":"(31) 99911-2233","cnh":"01234567890"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.statusRotulo").value("Ativo"));
    }

    @Test
    void listaMotoristasEmOrdemAlfabetica() throws Exception {
        motorista("Zilda");
        motorista("Ana");

        mvc.perform(get("/api/motoristas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[1].nome").value("Zilda"));
    }

    @Test
    void impedeExcluirMotoristaVinculadoACarga() throws Exception {
        Motorista motorista = motorista("João Silva");
        cargaComVinculos("CAR-001", null, motorista, null);

        mvc.perform(delete("/api/motoristas/" + motorista.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("vinculado a cargas")));

        assertEquals(1, motoristas.count());
    }

    @Test
    void excluiMotoristaSemVinculo() throws Exception {
        Motorista motorista = motorista("Sem cargas");

        mvc.perform(delete("/api/motoristas/" + motorista.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, motoristas.count());
    }
}
