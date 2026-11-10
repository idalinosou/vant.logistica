"use strict";

/* -------------------------------------------------------------------------
   Elementos e estado
   ------------------------------------------------------------------------- */

const el = {};
[
  "selo-modo", "atualizar-tudo", "aviso-geral",
  "vista-painel", "vista-cargas", "vista-frota", "vista-motoristas", "vista-clientes",
  "cartao-total", "cartao-transito", "cartao-pendentes", "cartao-atrasadas", "cartao-entregues",
  "corpo-ultimas", "recurso-veiculos", "recurso-veiculos-disponiveis",
  "recurso-motoristas", "recurso-motoristas-ativos", "recurso-clientes",
  "alerta-atrasadas", "texto-atrasadas",
  "chips-status", "busca-cargas", "botao-nova-carga",
  "painel-form-carga", "titulo-form-carga", "fechar-form-carga", "form-carga",
  "carga-codigo", "carga-origem", "carga-destino", "carga-previsao",
  "carga-cliente", "carga-motorista", "carga-veiculo",
  "salvar-carga", "cancelar-carga", "aviso-cargas", "contagem-cargas",
  "corpo-cargas", "paginacao-cargas",
  "painel-historico", "titulo-historico", "lista-historico", "fechar-historico",
  "titulo-form-veiculo", "form-veiculo", "veiculo-placa", "veiculo-modelo",
  "veiculo-tipo", "veiculo-capacidade", "veiculo-status",
  "salvar-veiculo", "cancelar-veiculo", "corpo-veiculos",
  "titulo-form-motorista", "form-motorista", "motorista-nome", "motorista-documento",
  "motorista-telefone", "motorista-cnh", "motorista-status",
  "salvar-motorista", "cancelar-motorista", "corpo-motoristas",
  "titulo-form-cliente", "form-cliente", "cliente-nome", "cliente-documento",
  "cliente-telefone", "cliente-email", "cliente-cidade",
  "salvar-cliente", "cancelar-cliente", "corpo-clientes",
  "toast", "toast-texto"
].forEach((id) => {
  el[id.replace(/-([a-z])/g, (_, letra) => letra.toUpperCase())] = document.getElementById(id);
});

const STATUS = {
  PENDENTE: { rotulo: "Pendente", classe: "status-pendente" },
  EM_TRANSITO: { rotulo: "Em trânsito", classe: "status-em-transito" },
  ENTREGUE: { rotulo: "Entregue", classe: "status-entregue" },
  CANCELADO: { rotulo: "Cancelado", classe: "status-cancelado" }
};

const STATUS_VEICULO = {
  DISPONIVEL: { rotulo: "Disponível", classe: "status-disponivel" },
  EM_OPERACAO: { rotulo: "Em operação", classe: "status-em-operacao" },
  MANUTENCAO: { rotulo: "Manutenção", classe: "status-manutencao" }
};

const STATUS_MOTORISTA = {
  ATIVO: { rotulo: "Ativo", classe: "status-ativo" },
  INATIVO: { rotulo: "Inativo", classe: "status-inativo" }
};

const TRANSICOES = {
  PENDENTE: ["EM_TRANSITO", "CANCELADO"],
  EM_TRANSITO: ["ENTREGUE", "CANCELADO"],
  ENTREGUE: [],
  CANCELADO: []
};

const CHAVE_DEMO = "vant.logistica.demo.v1";
const TAMANHO_PAGINA = 8;

const estado = {
  modo: null,
  vista: "painel",
  filtroStatus: "",
  busca: "",
  pagina: 1,
  cargas: { content: [], totalElements: 0, totalPages: 1, number: 0 },
  clientes: [],
  motoristas: [],
  veiculos: [],
  edicao: { carga: null, veiculo: null, motorista: null, cliente: null },
  confirmacoes: {},
  temporizadores: new Map(),
  timerBusca: null
};

/* -------------------------------------------------------------------------
   Demonstração (localStorage)
   ------------------------------------------------------------------------- */

const demo = (() => {
  let dados = carregar();

  function carregar() {
    try {
      const salvo = JSON.parse(localStorage.getItem(CHAVE_DEMO));
      if (salvo && Array.isArray(salvo.cargas) && Array.isArray(salvo.clientes)) {
        return salvo;
      }
    } catch (erro) {
      console.warn("Não foi possível ler a demonstração salva.", erro);
    }
    const semente = criarSemente();
    localStorage.setItem(CHAVE_DEMO, JSON.stringify(semente));
    return semente;
  }

  function salvar() {
    localStorage.setItem(CHAVE_DEMO, JSON.stringify(dados));
  }

  function erro(mensagem) {
    const falha = new Error(mensagem);
    falha.status = 409;
    throw falha;
  }

  function hojeIso() {
    return new Date().toISOString().slice(0, 10);
  }

  function isoDias(dias, hora = 9) {
    const data = new Date();
    data.setDate(data.getDate() + dias);
    data.setHours(hora, 0, 0, 0);
    return data.toISOString();
  }

  function previsao(dias) {
    return isoDias(dias).slice(0, 10);
  }

  function proximoId(lista) {
    return lista.reduce((maior, item) => Math.max(maior, item.id), 0) + 1;
  }

  function criarSemente() {
    const clientes = [
      { id: 1, nome: "Transportes Aurora", documento: "12.345.678/0001-90", telefone: "(31) 3222-4100", email: "contato@aurora.com.br", cidade: "Belo Horizonte - MG", criadoEm: isoDias(-40) },
      { id: 2, nome: "Mercado Central Ltda", documento: "98.765.432/0001-10", telefone: "(31) 3391-8820", email: "logistica@mercadocentral.com.br", cidade: "Contagem - MG", criadoEm: isoDias(-35) },
      { id: 3, nome: "Farmácia Vida", documento: "45.678.912/0001-55", telefone: "(31) 3532-1177", email: "compras@farmaciavida.com.br", cidade: "Betim - MG", criadoEm: isoDias(-30) },
      { id: 4, nome: "Distribuidora Norte", documento: "31.222.333/0001-08", telefone: "(31) 3777-9050", email: "operacoes@distnorte.com.br", cidade: "Sete Lagoas - MG", criadoEm: isoDias(-20) }
    ];
    const motoristas = [
      { id: 1, nome: "João Silva", documento: "123.456.789-00", telefone: "(31) 99911-2233", cnh: "01234567890", status: "ATIVO", criadoEm: isoDias(-60) },
      { id: 2, nome: "Marina Costa", documento: "234.567.890-11", telefone: "(31) 99822-4455", cnh: "09876543210", status: "ATIVO", criadoEm: isoDias(-55) },
      { id: 3, nome: "Diego Almeida", documento: "345.678.901-22", telefone: "(31) 99733-6677", cnh: "11223344556", status: "ATIVO", criadoEm: isoDias(-50) },
      { id: 4, nome: "Carla Souza", documento: "456.789.012-33", telefone: "(31) 99644-8899", cnh: "66554433221", status: "INATIVO", criadoEm: isoDias(-45) }
    ];
    const veiculos = [
      { id: 1, placa: "ABC1D23", modelo: "Caminhão Baú 01", tipo: "Caminhão", capacidade: "12 toneladas", status: "EM_OPERACAO", criadoEm: isoDias(-50) },
      { id: 2, placa: "XYZ9K88", modelo: "Carreta Graneleira 02", tipo: "Carreta", capacidade: "27 toneladas", status: "DISPONIVEL", criadoEm: isoDias(-45) },
      { id: 3, placa: "QWE4R56", modelo: "Van de entrega 03", tipo: "Van", capacidade: "1,5 tonelada", status: "DISPONIVEL", criadoEm: isoDias(-40) },
      { id: 4, placa: "JKL7M90", modelo: "Caminhão Refrigerado 04", tipo: "Caminhão", capacidade: "8 toneladas", status: "MANUTENCAO", criadoEm: isoDias(-35) }
    ];

    const historico = {};
    const cargas = [];
    let seq = 0;

    function registrar(carga, caminho) {
      const registros = [];
      let anterior = null;
      caminho.forEach((status, indice) => {
        registros.push({
          id: ++seq,
          statusAnterior: anterior,
          statusAnteriorRotulo: anterior ? STATUS[anterior].rotulo : null,
          novoStatus: status,
          novoStatusRotulo: STATUS[status].rotulo,
          dataAlteracao: isoDias(-(10 - carga.id), 8 + indice)
        });
        anterior = status;
      });
      historico[carga.id] = registros.reverse();
    }

    function novaCarga(codigo, clienteId, motoristaId, veiculoId, status, diasPrevisao, caminho) {
      const carga = {
        id: cargas.length + 1,
        codigo,
        origem: "",
        destino: "",
        clienteId,
        motoristaId,
        veiculoId,
        status,
        previsaoEntrega: previsao(diasPrevisao),
        criadaEm: isoDias(-(10 - (cargas.length + 1)), 8)
      };
      const rotas = {
        1: ["Betim - MG", "Belo Horizonte - MG"],
        2: ["Contagem - MG", "Sete Lagoas - MG"],
        3: ["Belo Horizonte - MG", "Betim - MG"],
        4: ["Sete Lagoas - MG", "Contagem - MG"],
        5: ["Contagem - MG", "Belo Horizonte - MG"],
        6: ["Betim - MG", "Sete Lagoas - MG"],
        7: ["Belo Horizonte - MG", "Contagem - MG"],
        8: ["Sete Lagoas - MG", "Betim - MG"]
      };
      [carga.origem, carga.destino] = rotas[carga.id];
      cargas.push(carga);
      registrar(carga, caminho);
    }

    novaCarga("CAR-001", 1, 1, 1, "EM_TRANSITO", 1, ["PENDENTE", "EM_TRANSITO"]);
    novaCarga("CAR-002", 2, 2, 2, "PENDENTE", 2, ["PENDENTE"]);
    novaCarga("CAR-003", 3, 3, 3, "ENTREGUE", -1, ["PENDENTE", "EM_TRANSITO", "ENTREGUE"]);
    novaCarga("CAR-004", 4, 1, 1, "PENDENTE", -3, ["PENDENTE"]);
    novaCarga("CAR-005", 1, 2, 3, "EM_TRANSITO", -2, ["PENDENTE", "EM_TRANSITO"]);
    novaCarga("CAR-006", 2, 3, 2, "ENTREGUE", -5, ["PENDENTE", "EM_TRANSITO", "ENTREGUE"]);
    novaCarga("CAR-007", 3, 1, 1, "CANCELADO", -4, ["PENDENTE", "CANCELADO"]);
    novaCarga("CAR-008", 4, 2, 3, "PENDENTE", 3, ["PENDENTE"]);

    return { clientes, motoristas, veiculos, cargas, historico, seq };
  }

  function clientePorId(id) {
    return dados.clientes.find((c) => c.id === id) || null;
  }

  function motoristaPorId(id) {
    return dados.motoristas.find((m) => m.id === id) || null;
  }

  function veiculoPorId(id) {
    return dados.veiculos.find((v) => v.id === id) || null;
  }

  function atrasada(carga) {
    return Boolean(carga.previsaoEntrega) && carga.previsaoEntrega < hojeIso()
      && carga.status !== "ENTREGUE" && carga.status !== "CANCELADO";
  }

  function cargaView(carga) {
    const cliente = clientePorId(carga.clienteId);
    const motorista = motoristaPorId(carga.motoristaId);
    const veiculo = veiculoPorId(carga.veiculoId);
    return {
      id: carga.id,
      codigo: carga.codigo,
      origem: carga.origem,
      destino: carga.destino,
      clienteId: carga.clienteId || null,
      clienteNome: cliente ? cliente.nome : null,
      motoristaId: carga.motoristaId || null,
      motoristaNome: motorista ? motorista.nome : null,
      veiculoId: carga.veiculoId || null,
      veiculoPlaca: veiculo ? veiculo.placa : null,
      veiculoModelo: veiculo ? veiculo.modelo : null,
      status: carga.status,
      statusRotulo: STATUS[carga.status].rotulo,
      previsaoEntrega: carga.previsaoEntrega,
      criadaEm: carga.criadaEm,
      atrasada: atrasada(carga)
    };
  }

  function veiculoView(veiculo) {
    return {
      ...veiculo,
      statusRotulo: STATUS_VEICULO[veiculo.status].rotulo
    };
  }

  function motoristaView(motorista) {
    return {
      ...motorista,
      statusRotulo: STATUS_MOTORISTA[motorista.status].rotulo
    };
  }

  function ordenarPorNome(lista) {
    return [...lista].sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"));
  }

  function listarCargas({ status, busca, pagina = 1, tamanho = TAMANHO_PAGINA }) {
    let itens = [...dados.cargas].sort((a, b) => b.id - a.id);
    if (status) {
      itens = itens.filter((carga) => carga.status === status);
    }
    if (busca && busca.trim()) {
      const termo = busca.trim().toLowerCase();
      itens = itens.filter((carga) => {
        const cliente = clientePorId(carga.clienteId);
        return [carga.codigo, carga.origem, carga.destino, cliente ? cliente.nome : ""]
          .join(" ").toLowerCase().includes(termo);
      });
    }
    const total = itens.length;
    const paginas = Math.max(1, Math.ceil(total / tamanho));
    const atual = Math.min(Math.max(pagina, 1), paginas);
    const conteudo = itens.slice((atual - 1) * tamanho, atual * tamanho).map(cargaView);
    return { content: conteudo, totalElements: total, totalPages: paginas, number: atual - 1, size: tamanho };
  }

  function criarCarga(pedido) {
    const codigo = pedido.codigo.trim();
    if (dados.cargas.some((carga) => carga.codigo.toLowerCase() === codigo.toLowerCase())) {
      erro(`Já existe uma carga com o código "${codigo}".`);
    }
    const carga = {
      id: proximoId(dados.cargas),
      codigo,
      origem: pedido.origem.trim(),
      destino: pedido.destino.trim(),
      clienteId: pedido.clienteId || null,
      motoristaId: pedido.motoristaId || null,
      veiculoId: pedido.veiculoId || null,
      status: "PENDENTE",
      previsaoEntrega: pedido.previsaoEntrega || null,
      criadaEm: new Date().toISOString()
    };
    dados.cargas.push(carga);
    dados.historico[carga.id] = [{
      id: ++dados.seq,
      statusAnterior: null,
      statusAnteriorRotulo: null,
      novoStatus: "PENDENTE",
      novoStatusRotulo: STATUS.PENDENTE.rotulo,
      dataAlteracao: carga.criadaEm
    }];
    salvar();
    return cargaView(carga);
  }

  function atualizarCarga(id, pedido) {
    const carga = dados.cargas.find((c) => c.id === id);
    if (!carga) {
      throw new Error(`Carga ${id} não encontrada.`);
    }
    const codigo = pedido.codigo.trim();
    if (dados.cargas.some((c) => c.id !== id && c.codigo.toLowerCase() === codigo.toLowerCase())) {
      erro(`Já existe outra carga com o código "${codigo}".`);
    }
    carga.codigo = codigo;
    carga.origem = pedido.origem.trim();
    carga.destino = pedido.destino.trim();
    carga.clienteId = pedido.clienteId || null;
    carga.motoristaId = pedido.motoristaId || null;
    carga.veiculoId = pedido.veiculoId || null;
    carga.previsaoEntrega = pedido.previsaoEntrega || null;
    salvar();
    return cargaView(carga);
  }

  function alterarStatus(id, status) {
    const carga = dados.cargas.find((c) => c.id === id);
    if (!carga) {
      throw new Error(`Carga ${id} não encontrada.`);
    }
    if (carga.status === status) {
      return cargaView(carga);
    }
    if (!TRANSICOES[carga.status].includes(status)) {
      erro(`Não é possível alterar o status de "${STATUS[carga.status].rotulo}" para "${STATUS[status].rotulo}".`);
    }
    const anterior = carga.status;
    carga.status = status;
    dados.historico[id].unshift({
      id: ++dados.seq,
      statusAnterior: anterior,
      statusAnteriorRotulo: STATUS[anterior].rotulo,
      novoStatus: status,
      novoStatusRotulo: STATUS[status].rotulo,
      dataAlteracao: new Date().toISOString()
    });
    salvar();
    return cargaView(carga);
  }

  function excluirCarga(id) {
    const indice = dados.cargas.findIndex((c) => c.id === id);
    if (indice < 0) {
      throw new Error(`Carga ${id} não encontrada.`);
    }
    dados.cargas.splice(indice, 1);
    delete dados.historico[id];
    salvar();
  }

  function historico(id) {
    return [...(dados.historico[id] || [])];
  }

  function painel() {
    const contar = (status) => dados.cargas.filter((c) => c.status === status).length;
    return {
      totalCargas: dados.cargas.length,
      pendentes: contar("PENDENTE"),
      emTransito: contar("EM_TRANSITO"),
      entregues: contar("ENTREGUE"),
      canceladas: contar("CANCELADO"),
      atrasadas: dados.cargas.filter(atrasada).length,
      veiculos: dados.veiculos.length,
      veiculosDisponiveis: dados.veiculos.filter((v) => v.status === "DISPONIVEL").length,
      motoristas: dados.motoristas.length,
      motoristasAtivos: dados.motoristas.filter((m) => m.status === "ATIVO").length,
      clientes: dados.clientes.length
    };
  }

  function criarCliente(pedido) {
    const cliente = {
      id: proximoId(dados.clientes),
      nome: pedido.nome.trim(),
      documento: pedido.documento || null,
      telefone: pedido.telefone || null,
      email: pedido.email || null,
      cidade: pedido.cidade || null,
      criadoEm: new Date().toISOString()
    };
    dados.clientes.push(cliente);
    salvar();
    return cliente;
  }

  function atualizarCliente(id, pedido) {
    const cliente = dados.clientes.find((c) => c.id === id);
    if (!cliente) {
      throw new Error(`Cliente ${id} não encontrado.`);
    }
    cliente.nome = pedido.nome.trim();
    cliente.documento = pedido.documento || null;
    cliente.telefone = pedido.telefone || null;
    cliente.email = pedido.email || null;
    cliente.cidade = pedido.cidade || null;
    salvar();
    return cliente;
  }

  function excluirCliente(id) {
    const cliente = dados.clientes.find((c) => c.id === id);
    if (!cliente) {
      throw new Error(`Cliente ${id} não encontrado.`);
    }
    if (dados.cargas.some((carga) => carga.clienteId === id)) {
      erro(`O cliente "${cliente.nome}" está vinculado a cargas e não pode ser excluído.`);
    }
    dados.clientes = dados.clientes.filter((c) => c.id !== id);
    salvar();
  }

  function criarMotorista(pedido) {
    const motorista = {
      id: proximoId(dados.motoristas),
      nome: pedido.nome.trim(),
      documento: pedido.documento || null,
      telefone: pedido.telefone || null,
      cnh: pedido.cnh || null,
      status: pedido.status || "ATIVO",
      criadoEm: new Date().toISOString()
    };
    dados.motoristas.push(motorista);
    salvar();
    return motoristaView(motorista);
  }

  function atualizarMotorista(id, pedido) {
    const motorista = dados.motoristas.find((m) => m.id === id);
    if (!motorista) {
      throw new Error(`Motorista ${id} não encontrado.`);
    }
    motorista.nome = pedido.nome.trim();
    motorista.documento = pedido.documento || null;
    motorista.telefone = pedido.telefone || null;
    motorista.cnh = pedido.cnh || null;
    motorista.status = pedido.status || "ATIVO";
    salvar();
    return motoristaView(motorista);
  }

  function excluirMotorista(id) {
    const motorista = dados.motoristas.find((m) => m.id === id);
    if (!motorista) {
      throw new Error(`Motorista ${id} não encontrado.`);
    }
    if (dados.cargas.some((carga) => carga.motoristaId === id)) {
      erro(`O motorista "${motorista.nome}" está vinculado a cargas e não pode ser excluído.`);
    }
    dados.motoristas = dados.motoristas.filter((m) => m.id !== id);
    salvar();
  }

  function criarVeiculo(pedido) {
    const placa = pedido.placa.trim().toUpperCase();
    if (dados.veiculos.some((v) => v.placa.toUpperCase() === placa)) {
      erro(`Já existe um veículo com a placa ${placa}.`);
    }
    const veiculo = {
      id: proximoId(dados.veiculos),
      placa,
      modelo: pedido.modelo.trim(),
      tipo: pedido.tipo || null,
      capacidade: pedido.capacidade || null,
      status: pedido.status || "DISPONIVEL",
      criadoEm: new Date().toISOString()
    };
    dados.veiculos.push(veiculo);
    salvar();
    return veiculoView(veiculo);
  }

  function atualizarVeiculo(id, pedido) {
    const veiculo = dados.veiculos.find((v) => v.id === id);
    if (!veiculo) {
      throw new Error(`Veículo ${id} não encontrado.`);
    }
    const placa = pedido.placa.trim().toUpperCase();
    if (dados.veiculos.some((v) => v.id !== id && v.placa.toUpperCase() === placa)) {
      erro(`Já existe um veículo com a placa ${placa}.`);
    }
    veiculo.placa = placa;
    veiculo.modelo = pedido.modelo.trim();
    veiculo.tipo = pedido.tipo || null;
    veiculo.capacidade = pedido.capacidade || null;
    veiculo.status = pedido.status || "DISPONIVEL";
    salvar();
    return veiculoView(veiculo);
  }

  function excluirVeiculo(id) {
    const veiculo = dados.veiculos.find((v) => v.id === id);
    if (!veiculo) {
      throw new Error(`Veículo ${id} não encontrado.`);
    }
    if (dados.cargas.some((carga) => carga.veiculoId === id)) {
      erro(`O veículo ${veiculo.placa} está vinculado a cargas e não pode ser excluído.`);
    }
    dados.veiculos = dados.veiculos.filter((v) => v.id !== id);
    salvar();
  }

  return {
    listarCargas,
    criarCarga,
    atualizarCarga,
    alterarStatus,
    excluirCarga,
    historico,
    painel,
    listarClientes: () => ordenarPorNome(dados.clientes),
    criarCliente,
    atualizarCliente,
    excluirCliente,
    listarMotoristas: () => ordenarPorNome(dados.motoristas).map(motoristaView),
    criarMotorista,
    atualizarMotorista,
    excluirMotorista,
    listarVeiculos: () => [...dados.veiculos].sort((a, b) => a.placa.localeCompare(b.placa)).map(veiculoView),
    criarVeiculo,
    atualizarVeiculo,
    excluirVeiculo
  };
})();

/* -------------------------------------------------------------------------
   API e adaptador de dados
   ------------------------------------------------------------------------- */

async function requisicao(url, opcoes = {}) {
  const resposta = await fetch(url, {
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    ...opcoes
  });
  if (resposta.status === 204) {
    return null;
  }
  const texto = await resposta.text();
  const corpo = texto ? JSON.parse(texto) : null;
  if (!resposta.ok) {
    const falha = new Error(corpo?.mensagem || "Não foi possível concluir a operação.");
    falha.campos = corpo?.campos || null;
    falha.status = resposta.status;
    throw falha;
  }
  return corpo;
}

const dados = {
  painel: () => estado.modo === "api" ? requisicao("api/painel") : Promise.resolve(demo.painel()),

  listarCargas: (filtros = {}) => {
    if (estado.modo !== "api") {
      return Promise.resolve(demo.listarCargas(filtros));
    }
    const parametros = new URLSearchParams();
    if (filtros.status) parametros.set("status", filtros.status);
    if (filtros.busca) parametros.set("busca", filtros.busca);
    parametros.set("pagina", String(filtros.pagina || 1));
    parametros.set("tamanho", String(filtros.tamanho || TAMANHO_PAGINA));
    return requisicao(`api/cargas?${parametros.toString()}`);
  },
  criarCarga: (pedido) => estado.modo === "api"
    ? requisicao("api/cargas", { method: "POST", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.criarCarga(pedido)),
  atualizarCarga: (id, pedido) => estado.modo === "api"
    ? requisicao(`api/cargas/${id}`, { method: "PUT", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.atualizarCarga(id, pedido)),
  alterarStatus: (id, status) => estado.modo === "api"
    ? requisicao(`api/cargas/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) })
    : Promise.resolve(demo.alterarStatus(id, status)),
  excluirCarga: (id) => estado.modo === "api"
    ? requisicao(`api/cargas/${id}`, { method: "DELETE" })
    : Promise.resolve(demo.excluirCarga(id)),
  historico: (id) => estado.modo === "api"
    ? requisicao(`api/cargas/${id}/historico`)
    : Promise.resolve(demo.historico(id)),

  listarClientes: () => estado.modo === "api" ? requisicao("api/clientes") : Promise.resolve(demo.listarClientes()),
  criarCliente: (pedido) => estado.modo === "api"
    ? requisicao("api/clientes", { method: "POST", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.criarCliente(pedido)),
  atualizarCliente: (id, pedido) => estado.modo === "api"
    ? requisicao(`api/clientes/${id}`, { method: "PUT", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.atualizarCliente(id, pedido)),
  excluirCliente: (id) => estado.modo === "api"
    ? requisicao(`api/clientes/${id}`, { method: "DELETE" })
    : Promise.resolve(demo.excluirCliente(id)),

  listarMotoristas: () => estado.modo === "api" ? requisicao("api/motoristas") : Promise.resolve(demo.listarMotoristas()),
  criarMotorista: (pedido) => estado.modo === "api"
    ? requisicao("api/motoristas", { method: "POST", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.criarMotorista(pedido)),
  atualizarMotorista: (id, pedido) => estado.modo === "api"
    ? requisicao(`api/motoristas/${id}`, { method: "PUT", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.atualizarMotorista(id, pedido)),
  excluirMotorista: (id) => estado.modo === "api"
    ? requisicao(`api/motoristas/${id}`, { method: "DELETE" })
    : Promise.resolve(demo.excluirMotorista(id)),

  listarVeiculos: () => estado.modo === "api" ? requisicao("api/veiculos") : Promise.resolve(demo.listarVeiculos()),
  criarVeiculo: (pedido) => estado.modo === "api"
    ? requisicao("api/veiculos", { method: "POST", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.criarVeiculo(pedido)),
  atualizarVeiculo: (id, pedido) => estado.modo === "api"
    ? requisicao(`api/veiculos/${id}`, { method: "PUT", body: JSON.stringify(pedido) })
    : Promise.resolve(demo.atualizarVeiculo(id, pedido)),
  excluirVeiculo: (id) => estado.modo === "api"
    ? requisicao(`api/veiculos/${id}`, { method: "DELETE" })
    : Promise.resolve(demo.excluirVeiculo(id))
};

/* -------------------------------------------------------------------------
   Utilidades de interface
   ------------------------------------------------------------------------- */

function escapar(valor) {
  return String(valor ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatarData(iso) {
  if (!iso) return "—";
  const [ano, mes, dia] = String(iso).slice(0, 10).split("-");
  return `${dia}/${mes}/${ano}`;
}

function formatarDataHora(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit"
  });
}

function pilula(status, mapa = STATUS) {
  const item = mapa[status];
  if (!item) return "";
  return `<span class="status-pill ${item.classe}">${escapar(item.rotulo)}</span>`;
}

let timerToast = null;

function mostrarToast(texto, tipo = "ok") {
  el.toastTexto.textContent = texto;
  el.toast.classList.toggle("erro", tipo === "erro");
  el.toast.hidden = false;
  clearTimeout(timerToast);
  timerToast = setTimeout(() => {
    el.toast.hidden = true;
  }, 3400);
}

function mostrarErro(falha) {
  mostrarToast(falha?.message || "Não foi possível concluir a operação.", "erro");
}

function agendar(chave, acao) {
  clearTimeout(estado.temporizadores.get(chave));
  estado.temporizadores.set(chave, setTimeout(acao, 8000));
}

function limparAgendamento(chave) {
  clearTimeout(estado.temporizadores.get(chave));
  estado.temporizadores.delete(chave);
}

function duasEtapas(chave, id, renderizar, executar) {
  if (estado.confirmacoes[chave] === id) {
    estado.confirmacoes[chave] = null;
    limparAgendamento(chave);
    executar();
    return;
  }
  estado.confirmacoes[chave] = id;
  renderizar();
  agendar(chave, () => {
    estado.confirmacoes[chave] = null;
    renderizar();
  });
}

/* -------------------------------------------------------------------------
   Navegação
   ------------------------------------------------------------------------- */

function abrirVista(nome) {
  estado.vista = nome;
  document.querySelectorAll("[data-vista]").forEach((botao) => {
    botao.classList.toggle("ativo", botao.dataset.vista === nome);
  });
  ["painel", "cargas", "frota", "motoristas", "clientes"].forEach((vista) => {
    el[`vista${vista.charAt(0).toUpperCase()}${vista.slice(1)}`].hidden = vista !== nome;
  });
  if (nome === "cargas") {
    carregarCargas();
  } else if (nome === "frota") {
    renderizarVeiculos();
  } else if (nome === "motoristas") {
    renderizarMotoristas();
  } else if (nome === "clientes") {
    renderizarClientes();
  } else {
    carregarPainel();
  }
}

/* -------------------------------------------------------------------------
   Painel
   ------------------------------------------------------------------------- */

async function carregarPainel() {
  try {
    const [resumo, ultimas] = await Promise.all([
      dados.painel(),
      dados.listarCargas({ pagina: 1, tamanho: 5 })
    ]);
    renderizarPainel(resumo);
    renderizarUltimas(ultimas.content);
  } catch (falha) {
    mostrarErro(falha);
  }
}

function renderizarPainel(resumo) {
  el.cartaoTotal.textContent = resumo.totalCargas;
  el.cartaoTransito.textContent = resumo.emTransito;
  el.cartaoPendentes.textContent = resumo.pendentes;
  el.cartaoAtrasadas.textContent = resumo.atrasadas;
  el.cartaoEntregues.textContent = resumo.entregues;
  el.recursoVeiculos.textContent = resumo.veiculos;
  el.recursoVeiculosDisponiveis.textContent = resumo.veiculosDisponiveis;
  el.recursoMotoristas.textContent = resumo.motoristas;
  el.recursoMotoristasAtivos.textContent = resumo.motoristasAtivos;
  el.recursoClientes.textContent = resumo.clientes;
  el.alertaAtrasadas.hidden = resumo.atrasadas === 0;
  if (resumo.atrasadas > 0) {
    el.textoAtrasadas.textContent = `${resumo.atrasadas} ${resumo.atrasadas === 1 ? "carga está" : "cargas estão"} com a previsão vencida e ainda em aberto.`;
  }
}

function renderizarUltimas(cargas) {
  if (!cargas.length) {
    el.corpoUltimas.innerHTML = '<tr><td colspan="4" class="vazio">Nenhuma carga cadastrada.</td></tr>';
    return;
  }
  el.corpoUltimas.innerHTML = cargas.map((carga) => `
    <tr>
      <td class="codigo">${escapar(carga.codigo)}</td>
      <td><div class="rota"><span>${escapar(carga.origem)} → ${escapar(carga.destino)}</span></div></td>
      <td>${escapar(carga.clienteNome || "—")}</td>
      <td>${pilula(carga.status)}</td>
    </tr>
  `).join("");
}

/* -------------------------------------------------------------------------
   Cargas
   ------------------------------------------------------------------------- */

function renderizarChips() {
  const opcoes = [{ valor: "", rotulo: "Todas" }].concat(
    Object.entries(STATUS).map(([valor, item]) => ({ valor, rotulo: item.rotulo }))
  );
  el.chipsStatus.innerHTML = opcoes.map((opcao) => `
    <button type="button" class="chip ${estado.filtroStatus === opcao.valor ? "ativo" : ""}"
      data-status="${opcao.valor}" aria-pressed="${estado.filtroStatus === opcao.valor}">
      ${escapar(opcao.rotulo)}
    </button>
  `).join("");
}

async function carregarCargas() {
  try {
    const pagina = await dados.listarCargas({
      status: estado.filtroStatus || null,
      busca: estado.busca,
      pagina: estado.pagina,
      tamanho: TAMANHO_PAGINA
    });
    estado.cargas = pagina;
    if (pagina.totalPages > 0) {
      estado.pagina = Math.min(estado.pagina, pagina.totalPages);
    }
    renderizarCargas(pagina);
  } catch (falha) {
    mostrarErro(falha);
  }
}

function acoesCarga(carga) {
  const botoes = [];
  if (carga.status === "PENDENTE") {
    botoes.push(`<button type="button" class="botao-acao" data-acao="status" data-status="EM_TRANSITO" data-id="${carga.id}">Iniciar</button>`);
  }
  if (carga.status === "EM_TRANSITO") {
    botoes.push(`<button type="button" class="botao-acao" data-acao="status" data-status="ENTREGUE" data-id="${carga.id}">Entregar</button>`);
  }
  if (carga.status === "PENDENTE" || carga.status === "EM_TRANSITO") {
    const confirmando = estado.confirmacoes.cancelarCarga === carga.id;
    botoes.push(`<button type="button" class="botao-acao botao-perigo ${confirmando ? "confirmando" : ""}" data-acao="cancelar" data-id="${carga.id}">${confirmando ? "Confirmar cancelamento" : "Cancelar"}</button>`);
  }
  botoes.push(`<button type="button" class="botao-acao" data-acao="editar" data-id="${carga.id}">Editar</button>`);
  botoes.push(`<button type="button" class="botao-acao" data-acao="historico" data-id="${carga.id}">Histórico</button>`);
  const confirmandoExclusao = estado.confirmacoes.excluirCarga === carga.id;
  botoes.push(`<button type="button" class="botao-acao botao-perigo ${confirmandoExclusao ? "confirmando" : ""}" data-acao="excluir" data-id="${carga.id}">${confirmandoExclusao ? "Confirmar exclusão" : "Excluir"}</button>`);
  return botoes.join("");
}

function renderizarCargas(pagina) {
  el.contagemCargas.textContent = `${pagina.totalElements} ${pagina.totalElements === 1 ? "registro" : "registros"}`;
  if (!pagina.content.length) {
    el.corpoCargas.innerHTML = '<tr><td colspan="7" class="vazio">Nenhuma carga encontrada com esses filtros.</td></tr>';
  } else {
    el.corpoCargas.innerHTML = pagina.content.map((carga) => `
      <tr>
        <td class="codigo">${escapar(carga.codigo)}</td>
        <td>
          <div class="rota">
            <span>${escapar(carga.origem)} → ${escapar(carga.destino)}</span>
            <small>Criada em ${formatarDataHora(carga.criadaEm)}</small>
          </div>
        </td>
        <td>${escapar(carga.clienteNome || "—")}</td>
        <td>
          <div class="recursos-carga">
            <span>${escapar(carga.motoristaNome || "Sem motorista")}</span>
            <span>${escapar(carga.veiculoPlaca || "Sem veículo")}${carga.veiculoModelo ? ` · ${escapar(carga.veiculoModelo)}` : ""}</span>
          </div>
        </td>
        <td class="previsao ${carga.atrasada ? "atrasada" : ""}">
          ${formatarData(carga.previsaoEntrega)}${carga.atrasada ? " · atrasada" : ""}
        </td>
        <td>${pilula(carga.status)}</td>
        <td><div class="acoes">${acoesCarga(carga)}</div></td>
      </tr>
    `).join("");
  }
  renderizarPaginacao(pagina);
}

function renderizarPaginacao(pagina) {
  const total = pagina.totalPages || 1;
  if (total <= 1) {
    el.paginacaoCargas.innerHTML = "";
    return;
  }
  const atual = (pagina.number || 0) + 1;
  const botoes = [];
  botoes.push(`<button type="button" data-pagina="${atual - 1}" ${atual === 1 ? "disabled" : ""} aria-label="Página anterior">‹</button>`);
  for (let numero = 1; numero <= total; numero++) {
    botoes.push(`<button type="button" data-pagina="${numero}" class="${numero === atual ? "ativo" : ""}" aria-current="${numero === atual}">${numero}</button>`);
  }
  botoes.push(`<button type="button" data-pagina="${atual + 1}" ${atual === total ? "disabled" : ""} aria-label="Próxima página">›</button>`);
  el.paginacaoCargas.innerHTML = botoes.join("");
}

function preencherSelect(select, itens, rotulo) {
  const valorAtual = select.value;
  select.innerHTML = `<option value="">Sem ${rotulo}</option>` + itens.map((item) =>
    `<option value="${item.id}">${escapar(item.nome || item.placa)}</option>`
  ).join("");
  if ([...select.options].some((opcao) => opcao.value === valorAtual)) {
    select.value = valorAtual;
  }
}

function atualizarSelects() {
  preencherSelect(el.cargaCliente, estado.clientes, "cliente");
  preencherSelect(el.cargaMotorista, estado.motoristas, "motorista");
  preencherSelect(el.cargaVeiculo, estado.veiculos, "veículo");
}

function abrirFormCarga(id = null) {
  estado.edicao.carga = id;
  const carga = id ? (estado.cargas.content.find((c) => c.id === id) || null) : null;
  el.painelFormCarga.hidden = false;
  el.tituloFormCarga.textContent = id ? "Editar carga" : "Nova carga";
  el.salvarCarga.textContent = id ? "Salvar alterações" : "Cadastrar carga";
  el.cancelarCarga.hidden = !id;
  el.formCarga.reset();
  if (carga) {
    el.cargaCodigo.value = carga.codigo;
    el.cargaOrigem.value = carga.origem;
    el.cargaDestino.value = carga.destino;
    el.cargaPrevisao.value = carga.previsaoEntrega || "";
    el.cargaCliente.value = carga.clienteId || "";
    el.cargaMotorista.value = carga.motoristaId || "";
    el.cargaVeiculo.value = carga.veiculoId || "";
  }
  el.painelFormCarga.scrollIntoView({ behavior: "smooth", block: "start" });
  if (!id) {
    el.cargaCodigo.focus();
  }
}

function fecharFormCarga() {
  estado.edicao.carga = null;
  el.formCarga.reset();
  el.painelFormCarga.hidden = true;
  el.cancelarCarga.hidden = true;
}

async function salvarCarga(evento) {
  evento.preventDefault();
  const codigo = el.cargaCodigo.value.trim();
  const origem = el.cargaOrigem.value.trim();
  const destino = el.cargaDestino.value.trim();
  if (!codigo || !origem || !destino) {
    mostrarToast("Preencha código, origem e destino.", "erro");
    (!codigo ? el.cargaCodigo : !origem ? el.cargaOrigem : el.cargaDestino).focus();
    return;
  }
  const pedido = {
    codigo,
    origem,
    destino,
    clienteId: el.cargaCliente.value ? Number(el.cargaCliente.value) : null,
    motoristaId: el.cargaMotorista.value ? Number(el.cargaMotorista.value) : null,
    veiculoId: el.cargaVeiculo.value ? Number(el.cargaVeiculo.value) : null,
    previsaoEntrega: el.cargaPrevisao.value || null
  };
  try {
    if (estado.edicao.carga) {
      await dados.atualizarCarga(estado.edicao.carga, pedido);
      mostrarToast(`Carga ${codigo} atualizada.`);
    } else {
      await dados.criarCarga(pedido);
      mostrarToast(`Carga ${codigo} cadastrada.`);
    }
    fecharFormCarga();
    await Promise.all([carregarCargas(), carregarPainel()]);
  } catch (falha) {
    mostrarErro(falha);
    if (falha.campos) {
      const mapa = { codigo: el.cargaCodigo, origem: el.cargaOrigem, destino: el.cargaDestino };
      const primeiro = Object.keys(falha.campos).map((campo) => mapa[campo]).find(Boolean);
      if (primeiro) primeiro.focus();
    }
  }
}

async function alterarStatusCarga(id, status) {
  try {
    await dados.alterarStatus(id, status);
    mostrarToast(`Status atualizado para ${STATUS[status].rotulo.toLowerCase()}.`);
    await Promise.all([carregarCargas(), carregarPainel()]);
  } catch (falha) {
    mostrarErro(falha);
  }
}

function excluirCarga(id) {
  duasEtapas("excluirCarga", id, () => renderizarCargas(estado.cargas), async () => {
    try {
      await dados.excluirCarga(id);
      mostrarToast("Carga excluída.");
      if (estado.historicoCargaId === id) {
        el.painelHistorico.hidden = true;
      }
      await Promise.all([carregarCargas(), carregarPainel()]);
    } catch (falha) {
      mostrarErro(falha);
    }
  });
}

function cancelarCarga(id) {
  duasEtapas("cancelarCarga", id, () => renderizarCargas(estado.cargas), () => alterarStatusCarga(id, "CANCELADO"));
}

async function abrirHistorico(id) {
  const carga = estado.cargas.content.find((c) => c.id === id);
  estado.historicoCargaId = id;
  el.painelHistorico.hidden = false;
  el.tituloHistorico.textContent = `Histórico — ${carga ? carga.codigo : `carga ${id}`}`;
  el.listaHistorico.innerHTML = '<li><span class="historico-texto">Carregando…</span></li>';
  try {
    const registros = await dados.historico(id);
    if (!registros.length) {
      el.listaHistorico.innerHTML = '<li><span class="historico-texto">Ainda não existem alterações de status.</span></li>';
    } else {
      el.listaHistorico.innerHTML = registros.map((registro) => `
        <li>
          <div class="historico-texto">
            ${registro.statusAnterior
              ? `${escapar(registro.statusAnteriorRotulo)} → <b>${escapar(registro.novoStatusRotulo)}</b>`
              : `Carga criada como <b>${escapar(registro.novoStatusRotulo)}</b>`}
          </div>
          <time>${formatarDataHora(registro.dataAlteracao)}</time>
        </li>
      `).join("");
    }
    el.painelHistorico.scrollIntoView({ behavior: "smooth", block: "start" });
  } catch (falha) {
    mostrarErro(falha);
  }
}

/* -------------------------------------------------------------------------
   Frota
   ------------------------------------------------------------------------- */

function renderizarVeiculos() {
  if (!estado.veiculos.length) {
    el.corpoVeiculos.innerHTML = '<tr><td colspan="6" class="vazio">Nenhum veículo cadastrado.</td></tr>';
    return;
  }
  el.corpoVeiculos.innerHTML = estado.veiculos.map((veiculo) => {
    const confirmando = estado.confirmacoes.excluirVeiculo === veiculo.id;
    return `
      <tr>
        <td class="codigo">${escapar(veiculo.placa)}</td>
        <td>${escapar(veiculo.modelo)}</td>
        <td>${escapar(veiculo.tipo || "—")}</td>
        <td>${escapar(veiculo.capacidade || "—")}</td>
        <td>${pilula(veiculo.status, STATUS_VEICULO)}</td>
        <td>
          <div class="acoes">
            <button type="button" class="botao-acao" data-acao="editar" data-id="${veiculo.id}">Editar</button>
            <button type="button" class="botao-acao botao-perigo ${confirmando ? "confirmando" : ""}" data-acao="excluir" data-id="${veiculo.id}">${confirmando ? "Confirmar exclusão" : "Excluir"}</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");
}

function abrirEdicaoVeiculo(id) {
  const veiculo = estado.veiculos.find((v) => v.id === id);
  if (!veiculo) return;
  estado.edicao.veiculo = id;
  el.veiculoPlaca.value = veiculo.placa;
  el.veiculoModelo.value = veiculo.modelo;
  el.veiculoTipo.value = veiculo.tipo || "";
  el.veiculoCapacidade.value = veiculo.capacidade || "";
  el.veiculoStatus.value = veiculo.status;
  el.tituloFormVeiculo.textContent = "Editar veículo";
  el.salvarVeiculo.textContent = "Salvar alterações";
  el.cancelarVeiculo.hidden = false;
  el.formVeiculo.scrollIntoView({ behavior: "smooth", block: "start" });
}

function cancelarEdicaoVeiculo() {
  estado.edicao.veiculo = null;
  el.formVeiculo.reset();
  el.tituloFormVeiculo.textContent = "Cadastrar veículo";
  el.salvarVeiculo.textContent = "Cadastrar veículo";
  el.cancelarVeiculo.hidden = true;
}

async function salvarVeiculo(evento) {
  evento.preventDefault();
  const placa = el.veiculoPlaca.value.trim();
  const modelo = el.veiculoModelo.value.trim();
  if (!placa || !modelo) {
    mostrarToast("Informe a placa e o modelo do veículo.", "erro");
    (!placa ? el.veiculoPlaca : el.veiculoModelo).focus();
    return;
  }
  const pedido = {
    placa,
    modelo,
    tipo: el.veiculoTipo.value.trim() || null,
    capacidade: el.veiculoCapacidade.value.trim() || null,
    status: el.veiculoStatus.value
  };
  try {
    if (estado.edicao.veiculo) {
      await dados.atualizarVeiculo(estado.edicao.veiculo, pedido);
      mostrarToast(`Veículo ${placa.toUpperCase()} atualizado.`);
    } else {
      await dados.criarVeiculo(pedido);
      mostrarToast(`Veículo ${placa.toUpperCase()} cadastrado.`);
    }
    cancelarEdicaoVeiculo();
    await atualizarDados();
  } catch (falha) {
    mostrarErro(falha);
  }
}

function excluirVeiculo(id) {
  duasEtapas("excluirVeiculo", id, renderizarVeiculos, async () => {
    try {
      await dados.excluirVeiculo(id);
      mostrarToast("Veículo excluído.");
      await atualizarDados();
    } catch (falha) {
      mostrarErro(falha);
    }
  });
}

/* -------------------------------------------------------------------------
   Motoristas
   ------------------------------------------------------------------------- */

function renderizarMotoristas() {
  if (!estado.motoristas.length) {
    el.corpoMotoristas.innerHTML = '<tr><td colspan="6" class="vazio">Nenhum motorista cadastrado.</td></tr>';
    return;
  }
  el.corpoMotoristas.innerHTML = estado.motoristas.map((motorista) => {
    const confirmando = estado.confirmacoes.excluirMotorista === motorista.id;
    return `
      <tr>
        <td class="codigo">${escapar(motorista.nome)}</td>
        <td>${escapar(motorista.documento || "—")}</td>
        <td>${escapar(motorista.telefone || "—")}</td>
        <td>${escapar(motorista.cnh || "—")}</td>
        <td>${pilula(motorista.status, STATUS_MOTORISTA)}</td>
        <td>
          <div class="acoes">
            <button type="button" class="botao-acao" data-acao="editar" data-id="${motorista.id}">Editar</button>
            <button type="button" class="botao-acao botao-perigo ${confirmando ? "confirmando" : ""}" data-acao="excluir" data-id="${motorista.id}">${confirmando ? "Confirmar exclusão" : "Excluir"}</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");
}

function abrirEdicaoMotorista(id) {
  const motorista = estado.motoristas.find((m) => m.id === id);
  if (!motorista) return;
  estado.edicao.motorista = id;
  el.motoristaNome.value = motorista.nome;
  el.motoristaDocumento.value = motorista.documento || "";
  el.motoristaTelefone.value = motorista.telefone || "";
  el.motoristaCnh.value = motorista.cnh || "";
  el.motoristaStatus.value = motorista.status;
  el.tituloFormMotorista.textContent = "Editar motorista";
  el.salvarMotorista.textContent = "Salvar alterações";
  el.cancelarMotorista.hidden = false;
  el.formMotorista.scrollIntoView({ behavior: "smooth", block: "start" });
}

function cancelarEdicaoMotorista() {
  estado.edicao.motorista = null;
  el.formMotorista.reset();
  el.tituloFormMotorista.textContent = "Cadastrar motorista";
  el.salvarMotorista.textContent = "Cadastrar motorista";
  el.cancelarMotorista.hidden = true;
}

async function salvarMotorista(evento) {
  evento.preventDefault();
  const nome = el.motoristaNome.value.trim();
  if (!nome) {
    mostrarToast("Informe o nome do motorista.", "erro");
    el.motoristaNome.focus();
    return;
  }
  const pedido = {
    nome,
    documento: el.motoristaDocumento.value.trim() || null,
    telefone: el.motoristaTelefone.value.trim() || null,
    cnh: el.motoristaCnh.value.trim() || null,
    status: el.motoristaStatus.value
  };
  try {
    if (estado.edicao.motorista) {
      await dados.atualizarMotorista(estado.edicao.motorista, pedido);
      mostrarToast(`Motorista ${nome} atualizado.`);
    } else {
      await dados.criarMotorista(pedido);
      mostrarToast(`Motorista ${nome} cadastrado.`);
    }
    cancelarEdicaoMotorista();
    await atualizarDados();
  } catch (falha) {
    mostrarErro(falha);
  }
}

function excluirMotorista(id) {
  duasEtapas("excluirMotorista", id, renderizarMotoristas, async () => {
    try {
      await dados.excluirMotorista(id);
      mostrarToast("Motorista excluído.");
      await atualizarDados();
    } catch (falha) {
      mostrarErro(falha);
    }
  });
}

/* -------------------------------------------------------------------------
   Clientes
   ------------------------------------------------------------------------- */

function renderizarClientes() {
  if (!estado.clientes.length) {
    el.corpoClientes.innerHTML = '<tr><td colspan="6" class="vazio">Nenhum cliente cadastrado.</td></tr>';
    return;
  }
  el.corpoClientes.innerHTML = estado.clientes.map((cliente) => {
    const confirmando = estado.confirmacoes.excluirCliente === cliente.id;
    return `
      <tr>
        <td class="codigo">${escapar(cliente.nome)}</td>
        <td>${escapar(cliente.documento || "—")}</td>
        <td>${escapar(cliente.telefone || "—")}</td>
        <td>${escapar(cliente.email || "—")}</td>
        <td>${escapar(cliente.cidade || "—")}</td>
        <td>
          <div class="acoes">
            <button type="button" class="botao-acao" data-acao="editar" data-id="${cliente.id}">Editar</button>
            <button type="button" class="botao-acao botao-perigo ${confirmando ? "confirmando" : ""}" data-acao="excluir" data-id="${cliente.id}">${confirmando ? "Confirmar exclusão" : "Excluir"}</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");
}

function abrirEdicaoCliente(id) {
  const cliente = estado.clientes.find((c) => c.id === id);
  if (!cliente) return;
  estado.edicao.cliente = id;
  el.clienteNome.value = cliente.nome;
  el.clienteDocumento.value = cliente.documento || "";
  el.clienteTelefone.value = cliente.telefone || "";
  el.clienteEmail.value = cliente.email || "";
  el.clienteCidade.value = cliente.cidade || "";
  el.tituloFormCliente.textContent = "Editar cliente";
  el.salvarCliente.textContent = "Salvar alterações";
  el.cancelarCliente.hidden = false;
  el.formCliente.scrollIntoView({ behavior: "smooth", block: "start" });
}

function cancelarEdicaoCliente() {
  estado.edicao.cliente = null;
  el.formCliente.reset();
  el.tituloFormCliente.textContent = "Cadastrar cliente";
  el.salvarCliente.textContent = "Cadastrar cliente";
  el.cancelarCliente.hidden = true;
}

async function salvarCliente(evento) {
  evento.preventDefault();
  const nome = el.clienteNome.value.trim();
  if (!nome) {
    mostrarToast("Informe o nome do cliente.", "erro");
    el.clienteNome.focus();
    return;
  }
  const pedido = {
    nome,
    documento: el.clienteDocumento.value.trim() || null,
    telefone: el.clienteTelefone.value.trim() || null,
    email: el.clienteEmail.value.trim() || null,
    cidade: el.clienteCidade.value.trim() || null
  };
  try {
    if (estado.edicao.cliente) {
      await dados.atualizarCliente(estado.edicao.cliente, pedido);
      mostrarToast(`Cliente ${nome} atualizado.`);
    } else {
      await dados.criarCliente(pedido);
      mostrarToast(`Cliente ${nome} cadastrado.`);
    }
    cancelarEdicaoCliente();
    await atualizarDados();
  } catch (falha) {
    mostrarErro(falha);
  }
}

function excluirCliente(id) {
  duasEtapas("excluirCliente", id, renderizarClientes, async () => {
    try {
      await dados.excluirCliente(id);
      mostrarToast("Cliente excluído.");
      await atualizarDados();
    } catch (falha) {
      mostrarErro(falha);
    }
  });
}

/* -------------------------------------------------------------------------
   Carga inicial e eventos
   ------------------------------------------------------------------------- */

async function carregarRecursos() {
  const [clientes, motoristas, veiculos] = await Promise.all([
    dados.listarClientes(),
    dados.listarMotoristas(),
    dados.listarVeiculos()
  ]);
  estado.clientes = clientes;
  estado.motoristas = motoristas;
  estado.veiculos = veiculos;
  atualizarSelects();
}

async function atualizarDados() {
  try {
    await carregarRecursos();
    renderizarVeiculos();
    renderizarMotoristas();
    renderizarClientes();
    await Promise.all([carregarPainel(), carregarCargas()]);
  } catch (falha) {
    mostrarErro(falha);
  }
}

function ligarEventos() {
  document.addEventListener("click", (evento) => {
    const alvoVista = evento.target.closest("[data-vista]");
    if (alvoVista) {
      abrirVista(alvoVista.dataset.vista);
      return;
    }
    const alvoIr = evento.target.closest("[data-ir]");
    if (alvoIr) {
      abrirVista(alvoIr.dataset.ir);
    }
  });

  el.atualizarTudo.addEventListener("click", async () => {
    await atualizarDados();
    mostrarToast("Dados atualizados.");
  });

  el.chipsStatus.addEventListener("click", (evento) => {
    const chip = evento.target.closest("[data-status]");
    if (!chip) return;
    estado.filtroStatus = chip.dataset.status;
    estado.pagina = 1;
    renderizarChips();
    carregarCargas();
  });

  el.buscaCargas.addEventListener("input", () => {
    clearTimeout(estado.timerBusca);
    estado.timerBusca = setTimeout(() => {
      estado.busca = el.buscaCargas.value;
      estado.pagina = 1;
      carregarCargas();
    }, 320);
  });

  el.paginacaoCargas.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-pagina]");
    if (!botao || botao.disabled) return;
    estado.pagina = Number(botao.dataset.pagina);
    carregarCargas();
  });

  el.botaoNovaCarga.addEventListener("click", () => abrirFormCarga());
  el.fecharFormCarga.addEventListener("click", fecharFormCarga);
  el.cancelarCarga.addEventListener("click", fecharFormCarga);
  el.formCarga.addEventListener("submit", salvarCarga);

  el.corpoCargas.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    const id = Number(botao.dataset.id);
    const acao = botao.dataset.acao;
    if (acao === "editar") {
      const carga = estado.cargas.content.find((c) => c.id === id);
      if (carga) abrirFormCargaCompleto(carga);
    } else if (acao === "historico") {
      abrirHistorico(id);
    } else if (acao === "status") {
      alterarStatusCarga(id, botao.dataset.status);
    } else if (acao === "cancelar") {
      cancelarCarga(id);
    } else if (acao === "excluir") {
      excluirCarga(id);
    }
  });

  el.fecharHistorico.addEventListener("click", () => {
    el.painelHistorico.hidden = true;
    estado.historicoCargaId = null;
  });

  el.formVeiculo.addEventListener("submit", salvarVeiculo);
  el.cancelarVeiculo.addEventListener("click", cancelarEdicaoVeiculo);
  el.corpoVeiculos.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    const id = Number(botao.dataset.id);
    if (botao.dataset.acao === "editar") abrirEdicaoVeiculo(id);
    if (botao.dataset.acao === "excluir") excluirVeiculo(id);
  });

  el.formMotorista.addEventListener("submit", salvarMotorista);
  el.cancelarMotorista.addEventListener("click", cancelarEdicaoMotorista);
  el.corpoMotoristas.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    const id = Number(botao.dataset.id);
    if (botao.dataset.acao === "editar") abrirEdicaoMotorista(id);
    if (botao.dataset.acao === "excluir") excluirMotorista(id);
  });

  el.formCliente.addEventListener("submit", salvarCliente);
  el.cancelarCliente.addEventListener("click", cancelarEdicaoCliente);
  el.corpoClientes.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    const id = Number(botao.dataset.id);
    if (botao.dataset.acao === "editar") abrirEdicaoCliente(id);
    if (botao.dataset.acao === "excluir") excluirCliente(id);
  });
}

function abrirFormCargaCompleto(carga) {
  abrirFormCarga(carga.id);
}

async function detectarModo() {
  try {
    const resposta = await fetch("api/painel", { headers: { Accept: "application/json" } });
    if (resposta.ok) {
      estado.modo = "api";
      el.seloModo.textContent = "API conectada";
      el.seloModo.classList.add("api");
      el.avisoGeral.textContent = "";
      return;
    }
  } catch (erro) {
    // sem API: segue em demonstração
  }
  estado.modo = "demo";
  el.seloModo.textContent = "Modo demonstração";
  el.seloModo.classList.add("demo");
  el.avisoGeral.textContent = "Demonstração sem servidor: os dados ficam salvos apenas neste navegador.";
}

async function iniciar() {
  renderizarChips();
  ligarEventos();
  await detectarModo();
  await atualizarDados();
}

iniciar();
