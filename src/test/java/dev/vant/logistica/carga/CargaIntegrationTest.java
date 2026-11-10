package dev.vant.logistica.carga;

import dev.vant.logistica.BaseIntegracao;
import dev.vant.logistica.cliente.Cliente;
import dev.vant.logistica.motorista.Motorista;
import dev.vant.logistica.veiculo.Veiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CargaIntegrationTest extends BaseIntegracao {

    @Autowired
    private HistoricoCargaRepository historicos;

    @Test
    void criaCargaComVinculosEStatusInicial() throws Exception {
        Cliente cliente = cliente("Transportes Aurora");
        Motorista motorista = motorista("João Silva");
        Veiculo veiculo = veiculo("ABC1D23");

        String corpo = """
                {"codigo":"CAR-100","origem":"Betim - MG","destino":"Belo Horizonte - MG",
                 "clienteId":%d,"motoristaId":%d,"veiculoId":%d,"previsaoEntrega":"2030-01-10"}
                """.formatted(cliente.getId(), motorista.getId(), veiculo.getId());

        mvc.perform(post("/api/cargas").contentType(APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo").value("CAR-100"))
                .andExpect(jsonPath("$.clienteNome").value("Transportes Aurora"))
                .andExpect(jsonPath("$.motoristaNome").value("João Silva"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1D23"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.statusRotulo").value("Pendente"))
                .andExpect(jsonPath("$.atrasada").value(false));

        assertEquals(1, cargas.count());
        assertEquals(1, historicos.count());
    }

    @Test
    void rejeitaCodigoDuplicadoIgnorandoMaiusculas() throws Exception {
        carga("CAR-100", StatusCarga.PENDENTE, LocalDate.now().plusDays(2));

        mvc.perform(post("/api/cargas").contentType(APPLICATION_JSON)
                        .content("""
                                {"codigo":"car-100","origem":"Betim - MG","destino":"Contagem - MG"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Já existe uma carga")));
    }

    @Test
    void rejeitaCamposObrigatorios() throws Exception {
        mvc.perform(post("/api/cargas").contentType(APPLICATION_JSON)
                        .content("""
                                {"codigo":"","origem":"","destino":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.codigo").exists())
                .andExpect(jsonPath("$.campos.origem").exists())
                .andExpect(jsonPath("$.campos.destino").exists());
    }

    @Test
    void listaEFiltraPorStatus() throws Exception {
        carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));
        carga("CAR-002", StatusCarga.EM_TRANSITO, LocalDate.now().plusDays(2));

        mvc.perform(get("/api/cargas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/api/cargas").param("status", "EM_TRANSITO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("CAR-002"));
    }

    @Test
    void buscaPorTrechoDestinoOuNomeDoCliente() throws Exception {
        Cliente cliente = cliente("Mercado Central");
        cargaComVinculos("CAR-001", cliente, null, null);
        cargas.save(new Carga("CAR-002", "Contagem - MG", "Sete Lagoas - MG",
                null, null, null, StatusCarga.PENDENTE, LocalDate.now().plusDays(1)));

        mvc.perform(get("/api/cargas").param("busca", "sete lagoas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("CAR-002"));

        mvc.perform(get("/api/cargas").param("busca", "mercado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].codigo").value("CAR-001"));
    }

    @Test
    void atualizaDadosDaCargaMantendoStatus() throws Exception {
        Carga carga = carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));

        mvc.perform(put("/api/cargas/" + carga.getId()).contentType(APPLICATION_JSON)
                        .content("""
                                {"codigo":"CAR-001","origem":"Betim - MG","destino":"Sete Lagoas - MG",
                                 "previsaoEntrega":"2030-02-01","status":"PENDENTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destino").value("Sete Lagoas - MG"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void registraHistoricoAoMudarStatus() throws Exception {
        Carga carga = carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));

        mvc.perform(patch("/api/cargas/" + carga.getId() + "/status").contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"EM_TRANSITO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_TRANSITO"));

        mvc.perform(get("/api/cargas/" + carga.getId() + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].statusAnterior").value("PENDENTE"))
                .andExpect(jsonPath("$[0].novoStatus").value("EM_TRANSITO"));
    }

    @Test
    void impedeSaltoDeStatus() throws Exception {
        Carga carga = carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));

        mvc.perform(patch("/api/cargas/" + carga.getId() + "/status").contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"ENTREGUE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Não é possível alterar o status")));
    }

    @Test
    void impedeReabrirCargaFinalizada() throws Exception {
        Carga carga = carga("CAR-001", StatusCarga.ENTREGUE, LocalDate.now().minusDays(1));

        mvc.perform(patch("/api/cargas/" + carga.getId() + "/status").contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"EM_TRANSITO"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void excluiCargaEJuntoComHistorico() throws Exception {
        Carga carga = carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().plusDays(1));

        mvc.perform(delete("/api/cargas/" + carga.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, cargas.count());
        assertEquals(0, historicos.count());
    }

    @Test
    void retornaErroParaCargaInexistente() throws Exception {
        mvc.perform(get("/api/cargas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcaCargaAtrasadaSomenteQuandoNaoFinalizada() throws Exception {
        Carga atrasada = carga("CAR-001", StatusCarga.PENDENTE, LocalDate.now().minusDays(2));
        Carga entregue = carga("CAR-002", StatusCarga.ENTREGUE, LocalDate.now().minusDays(2));

        mvc.perform(get("/api/cargas/" + atrasada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasada").value(true));

        mvc.perform(get("/api/cargas/" + entregue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasada").value(false));
    }

    @Test
    void rejeitaVinculoInexistente() throws Exception {
        mvc.perform(post("/api/cargas").contentType(APPLICATION_JSON)
                        .content("""
                                {"codigo":"CAR-001","origem":"Betim - MG","destino":"Contagem - MG","clienteId":999}
                                """))
                .andExpect(status().isNotFound());
    }
}
