package dev.vant.logistica.cliente;

import dev.vant.logistica.BaseIntegracao;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteIntegrationTest extends BaseIntegracao {

    @Test
    void criaEListaCliente() throws Exception {
        mvc.perform(post("/api/clientes").contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Transportes Aurora","documento":"12.345.678/0001-90",
                                 "telefone":"(31) 3222-4100","email":"contato@aurora.com.br","cidade":"Belo Horizonte - MG"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Transportes Aurora"));

        mvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void validaNomeEEmail() throws Exception {
        mvc.perform(post("/api/clientes").contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void atualizaCliente() throws Exception {
        Cliente cliente = cliente("Nome antigo");

        mvc.perform(put("/api/clientes/" + cliente.getId()).contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Nome novo","cidade":"Betim - MG"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome novo"))
                .andExpect(jsonPath("$.cidade").value("Betim - MG"));
    }

    @Test
    void impedeExcluirClienteVinculadoACarga() throws Exception {
        Cliente cliente = cliente("Transportes Aurora");
        cargaComVinculos("CAR-001", cliente, null, null);

        mvc.perform(delete("/api/clientes/" + cliente.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("vinculado a cargas")));

        assertEquals(1, clientes.count());
    }

    @Test
    void excluiClienteSemVinculo() throws Exception {
        Cliente cliente = cliente("Sem cargas");

        mvc.perform(delete("/api/clientes/" + cliente.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, clientes.count());
    }
}
