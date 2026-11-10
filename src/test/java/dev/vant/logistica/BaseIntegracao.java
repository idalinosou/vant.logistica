package dev.vant.logistica;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vant.logistica.carga.Carga;
import dev.vant.logistica.carga.CargaRepository;
import dev.vant.logistica.carga.StatusCarga;
import dev.vant.logistica.cliente.Cliente;
import dev.vant.logistica.cliente.ClienteRepository;
import dev.vant.logistica.motorista.Motorista;
import dev.vant.logistica.motorista.MotoristaRepository;
import dev.vant.logistica.motorista.StatusMotorista;
import dev.vant.logistica.veiculo.StatusVeiculo;
import dev.vant.logistica.veiculo.Veiculo;
import dev.vant.logistica.veiculo.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegracao {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected ClienteRepository clientes;

    @Autowired
    protected MotoristaRepository motoristas;

    @Autowired
    protected VeiculoRepository veiculos;

    @Autowired
    protected CargaRepository cargas;

    protected Cliente cliente(String nome) {
        return clientes.save(new Cliente(nome, "000.000.000-00", "(31) 90000-0000",
                "cliente@teste.com", "Belo Horizonte - MG"));
    }

    protected Motorista motorista(String nome) {
        return motoristas.save(new Motorista(nome, "111.111.111-11", "(31) 91111-1111",
                "12345678900", StatusMotorista.ATIVO));
    }

    protected Veiculo veiculo(String placa) {
        return veiculos.save(new Veiculo(placa, "Caminhão de teste", "Caminhão",
                "10 toneladas", StatusVeiculo.DISPONIVEL));
    }

    protected Carga carga(String codigo, StatusCarga status, LocalDate previsao) {
        return cargas.save(new Carga(codigo, "Betim - MG", "Belo Horizonte - MG",
                null, null, null, status, previsao));
    }

    protected Carga cargaComVinculos(String codigo, Cliente cliente, Motorista motorista, Veiculo veiculo) {
        return cargas.save(new Carga(codigo, "Betim - MG", "Contagem - MG",
                cliente, motorista, veiculo, StatusCarga.PENDENTE, LocalDate.now().plusDays(1)));
    }

    protected String json(Object valor) throws Exception {
        return json.writeValueAsString(valor);
    }
}
