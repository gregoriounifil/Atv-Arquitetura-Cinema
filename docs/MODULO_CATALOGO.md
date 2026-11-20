# Módulo Catálogo (Dupla 1)

Bounded context `br.com.cinema.catalogo`. É o dono do **que está em cartaz e
onde**: cadastra filmes e salas e programa as sessões. Quando uma sessão é
programada, publica o `SessaoCriadaEvent`, que o restante do sistema usa para
reagir sem conhecer o Catálogo.

---

## 1. Papel no ciclo de eventos

```
Catálogo ──SessaoCriadaEvent──▶ (Reservas: mapa de assentos da sessão)
```

O Catálogo só importa de `br.com.cinema.shared.events`. Nenhuma classe de
`reservas` ou `pagamentos` é usada aqui, e isso é verificado em tempo de build
pelo `ApplicationModules.verify()` do Spring Modulith.

---

## 2. Estrutura (Hexagonal)

| Camada | Conteúdo |
|---|---|
| `domain/model` | `Filme`, `Sala`, `Sessao`, `ClassificacaoIndicativa` |
| `domain/exception` | `CatalogoDomainException`, `RecursoNaoEncontradoException` |
| `application/port/in` | `GerenciarFilmesUseCase`, `GerenciarSalasUseCase`, `ProgramarSessaoUseCase`, `ConsultarSessoesUseCase` |
| `application/port/out` | `FilmeRepositoryPort`, `SalaRepositoryPort`, `SessaoRepositoryPort`, `PublicarEventoCatalogoPort` |
| `application/service` | `FilmeService`, `SalaService`, `SessaoService` |
| `infrastructure/adapter/in/web` | `FilmeController`, `SalaController`, `SessaoController`, `CatalogoExceptionHandler` |
| `infrastructure/adapter/out/persistence` | entidades JPA, repositórios Spring Data e `*PersistenceAdapter` de filmes, salas e sessões |
| `infrastructure/adapter/out/messaging` | `CatalogoEventPublisherAdapter` |

O domínio é Java puro (regra `dominioDeveSerPuro` do `ArchitectureGuardTest`) e
os controllers só falam com interfaces de `application/port/in`.

---

## 3. Regras de domínio

| Entidade | Regras |
|---|---|
| `Filme` | título obrigatório; duração entre 1 e 600 minutos; classificação obrigatória |
| `Sala` | nome obrigatório e único; capacidade entre 1 e 500 lugares |
| `Sessao` | início depois de "agora"; preço base maior que zero; **fim = início + duração do filme** |

`Sessao.programar(filme, sala, inicio, precoBase, agora)` recebe o `agora` por
parâmetro, o que mantém o domínio determinístico e fácil de testar. O fim nunca
é informado pelo cliente: é calculado pelo domínio.

---

## 4. A invariante: sem sobreposição na mesma sala

Duas sessões não podem ocupar a mesma sala ao mesmo tempo. A regra é garantida
**no banco**, não no serviço — uma checagem em Java perderia a corrida entre
duas requisições simultâneas:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE sessoes ADD CONSTRAINT ex_sessoes_sala_sem_sobreposicao
    EXCLUDE USING gist (sala_id WITH =, tstzrange(inicio, fim) WITH &&);
```

- `sala_id WITH =` limita a restrição a cada sala: o mesmo horário em outra sala
  é permitido.
- `tstzrange(inicio, fim)` é semiaberto `[inicio, fim)`: uma sessão pode começar
  exatamente quando a anterior termina.
- A extensão `btree_gist` é o que permite combinar igualdade (`=`) e
  sobreposição (`&&`) no mesmo índice.

O `SessaoPersistenceAdapter` traduz a `DataIntegrityViolationException` da
restrição em `CatalogoDomainException("A sala ja tem uma sessao nesse
horario.")`, devolvida pela API como `422`.

---

## 5. Evento publicado

```java
record SessaoCriadaEvent(UUID sessaoId, UUID filmeId, UUID salaId,
                         Instant inicio, Instant fim,
                         BigDecimal precoBase, int capacidadeSala)
```

Fica em `shared/events`. O `SessaoService` grava a sessão e publica o evento na
**mesma transação** (`@Transactional`), como o `ReservaService` faz: se a
gravação falhar (por exemplo, por sobreposição), nenhum evento sai.
`capacidadeSala` vai no evento para que o consumidor monte o mapa de assentos
sem precisar consultar o Catálogo.

---

## 6. Banco — migrations V7 e V8

- `V7__catalogo_filmes_e_salas.sql` recria `filmes` e `salas` com identidade
  UUID, alinhadas com `reservas` (V6) e `pagamentos` (V5). Remove as tabelas
  antigas em `BIGSERIAL`, incluindo `sessoes` e `assentos`.
- `V8__catalogo_sessoes.sql` recria `sessoes` com UUID, `preco_base NUMERIC(10,2)
  CHECK (> 0)`, `CHECK (fim > inicio)` e a restrição `EXCLUDE` da seção 4.

`sessoes` tem FK para `filmes` e `salas` porque são do mesmo módulo. Nenhuma
tabela de outro módulo aponta para `sessoes`: Reservas guarda só o UUID lógico
`sessao_id`.

---

## 7. API

| Método | Rota | Efeito |
|---|---|---|
| `POST` | `/api/v1/filmes` | Cadastra um filme |
| `GET` | `/api/v1/filmes` | Lista os filmes (por título) |
| `GET` | `/api/v1/filmes/{filmeId}` | Consulta um filme |
| `POST` | `/api/v1/salas` | Cadastra uma sala |
| `GET` | `/api/v1/salas` | Lista as salas (por nome) |
| `GET` | `/api/v1/salas/{salaId}` | Consulta uma sala |
| `POST` | `/api/v1/sessoes` | Programa a sessão e publica `SessaoCriadaEvent` |
| `GET` | `/api/v1/sessoes/{sessaoId}` | Consulta uma sessão |
| `GET` | `/api/v1/sessoes?filmeId=...` | Lista as sessões de um filme (por início) |

Erros (`CatalogoExceptionHandler`, RFC 7807): `404` para recurso inexistente e
`422` para violação de regra de negócio (inclusive sobreposição de sessões).

---

## 8. Testes

- `FilmeESalaTest` — regras de domínio de filme e sala.
- `SessaoTest` — fim = início + duração, início no passado recusado, preço zero
  recusado.
- `CatalogoFilmesSalasIntegrationTest` — filmes e salas até o Postgres.
- `CatalogoSessoesIntegrationTest` (`@SpringBootTest` + `@RecordApplicationEvents`)
  — programar publica exatamente um `SessaoCriadaEvent`; sessão sobreposta na
  mesma sala é recusada (sem evento); sessão colada na anterior e mesma faixa
  em outra sala são aceitas; filme ou sala inexistente lança
  `RecursoNaoEncontradoException`.
- `ArchitectureGuardTest` — fronteiras entre módulos e pureza do domínio.
