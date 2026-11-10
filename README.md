# vant.logistica

Central logística full stack: API REST em Java 21 + Spring Boot e interface web em HTML, CSS e JavaScript puro. O sistema controla cargas com histórico de status, frota, motoristas e clientes, além de indicadores de atraso na operação.

**Demonstração:** https://idalinosou.github.io/vant.logistica/

A demonstração no GitHub Pages funciona sem servidor: os dados de exemplo ficam salvos no `localStorage` do navegador. A API real roda em Spring Boot e pode ser executada localmente.

## Funcionalidades

- Painel com total de cargas, pendentes, em trânsito, entregues, atrasadas e situação da frota e da equipe
- Cargas com busca por código, rota ou cliente, filtro por status e paginação
- Cadastro e edição de cargas com vínculo a cliente, motorista e veículo
- Fluxo de status: pendente → em trânsito → entregue, com cancelamento permitido antes da entrega
- Histórico com todas as mudanças de status de cada carga
- Previsão de entrega vencida destacada como atraso
- Cadastro, edição e exclusão de veículos, motoristas e clientes
- Bloqueio de exclusão de clientes, motoristas e veículos vinculados a cargas
- Validações, mensagens de erro claras e confirmação em duas etapas para exclusões
- Interface responsiva, acessível e com tema escuro

## Stack

- Java 21
- Spring Boot 3.4
- Spring Web, Spring Data JPA e Bean Validation
- H2 Database
- Maven
- HTML, CSS e JavaScript sem frameworks

## Como rodar

Pré-requisitos: Java 21 e Maven.

```bash
mvn spring-boot:run
```

Depois acesse http://localhost:8083.

O banco fica em `./dados/vantlogistica.mv.db` e já começa com cargas, veículos, motoristas e clientes de exemplo. Para recomeçar do zero, pare a aplicação e apague a pasta `dados`. O console do H2 fica em http://localhost:8083/h2-console.

## Testes

```bash
mvn test
```

São 30 testes de integração com MockMvc cobrindo cargas, histórico, transições de status, filtros, paginação, veículos, motoristas, clientes, bloqueios de exclusão e agregações do painel.

## API

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/painel` | Indicadores da operação |
| GET | `/api/cargas` | Lista cargas com `status`, `busca`, `pagina` e `tamanho` |
| GET | `/api/cargas/{id}` | Busca uma carga |
| POST | `/api/cargas` | Cadastra uma carga |
| PUT | `/api/cargas/{id}` | Atualiza os dados da carga |
| PATCH | `/api/cargas/{id}/status` | Altera o status da carga |
| GET | `/api/cargas/{id}/historico` | Lista o histórico de status |
| DELETE | `/api/cargas/{id}` | Exclui uma carga |
| GET | `/api/veiculos` | Lista a frota |
| POST | `/api/veiculos` | Cadastra um veículo |
| PUT | `/api/veiculos/{id}` | Atualiza um veículo |
| DELETE | `/api/veiculos/{id}` | Exclui um veículo sem cargas |
| GET | `/api/motoristas` | Lista os motoristas |
| POST | `/api/motoristas` | Cadastra um motorista |
| PUT | `/api/motoristas/{id}` | Atualiza um motorista |
| DELETE | `/api/motoristas/{id}` | Exclui um motorista sem cargas |
| GET | `/api/clientes` | Lista os clientes |
| POST | `/api/clientes` | Cadastra um cliente |
| PUT | `/api/clientes/{id}` | Atualiza um cliente |
| DELETE | `/api/clientes/{id}` | Exclui um cliente sem cargas |

## Regras de negócio

- Código de carga e placa de veículo são únicos.
- Pendente pode ir para em trânsito ou cancelado; em trânsito pode ir para entregue ou cancelado.
- Entregue e cancelado são estados finais.
- Toda mudança de status é registrada no histórico da carga.
- Carga com previsão vencida e ainda pendente ou em trânsito aparece como atrasada.
- Cliente, motorista e veículo vinculados a cargas não podem ser excluídos.

## Estrutura

```
src/main/java/dev/vant/logistica
├── carga          cargas, histórico, status e endpoints
├── veiculo        frota e endpoints
├── motorista      equipe e endpoints
├── cliente        carteira de clientes e endpoints
├── painel         indicadores da operação
├── dados          carga inicial de exemplo
└── erro           tratamento centralizado de erros
```

A interface fica em `src/main/resources/static` e é copiada para `docs/` na publicação do GitHub Pages. A versão original do projeto, em Angular e PostgreSQL, está preservada em `legacy/`.
