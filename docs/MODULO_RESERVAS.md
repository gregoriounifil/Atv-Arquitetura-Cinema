# Módulo Reservas (Dupla 2)

Bounded context `br.com.cinema.reservas`. É o módulo que **fecha o ciclo de
eventos do sistema**: publica o evento que inicia a cobrança e reage aos três
eventos de desfecho devolvidos pelo módulo Pagamentos.

Escopo deliberadamente enxuto — 16 arquivos, ~620 linhas. O objetivo é
demonstrar a **estrutura e a aplicação do estilo arquitetural**, não a
quantidade de funcionalidades.

---

## 1. O ciclo event-driven

```
Reservas ──AssentoReservadoEvent──▶ Pagamentos
    ▲                                    │
    └───── PagamentoAprovadoEvent ────────┘
           PagamentoRecusadoEvent
           PagamentoExpiradoEvent
```

Nenhum dos dois módulos importa classes do outro. A conversa acontece
exclusivamente pelos contratos em `shared/events`, e isso é verificado em
tempo de build pelo `ApplicationModules.verify()` do Spring Modulith.

---

## 2. Estrutura (Hexagonal)

| Camada | Arquivos | Conteúdo |
|---|---|---|
| `domain` | 3 | `Reserva`, `StatusReserva`, `ReservaDomainException` |
| `application/port/in` | 2 | `ReservarAssentoUseCase`, `DesfechoPagamentoUseCase` |
| `application/port/out` | 2 | `ReservaRepositoryPort`, `PublicarEventoReservaPort` |
| `application/service` | 1 | `ReservaService` |
| `infrastructure/adapter/in` | 2 | `ReservaController` (web), `PagamentoEventsListener` (mensageria) |
| `infrastructure/adapter/out` | 4 | persistência JPA e publisher de eventos |

O domínio compila sem **nenhuma** dependência no classpath — é a prova prática
da regra `dominioDeveSerPuro` do `ArchitectureGuardTest`.

---

## 3. Máquina de estados

```
PENDENTE ──confirmar()──▶ CONFIRMADA   (terminal)
         └─cancelar()──▶ CANCELADA    (terminal)
```

As transições são **idempotentes para o próprio estado**: chamar `confirmar()`
numa reserva já confirmada não faz nada e não lança erro. Isso é necessário
porque o outbox pode reentregar o mesmo evento. Transição a partir de um estado
terminal, essa sim, lança `ReservaDomainException`.

---

## 4. A invariante: um assento, uma reserva

Um assento não pode ter duas reservas vivas na mesma sessão. A regra é
garantida **no banco**, não no serviço — uma checagem em Java perderia a
corrida entre dois cliques simultâneos:

```sql
CREATE UNIQUE INDEX uk_reserva_ativa_por_assento
    ON reservas (sessao_id, assento_id)
    WHERE status IN ('PENDENTE', 'CONFIRMADA');
```

O `WHERE` é o ponto central. Um índice único comum travaria o assento para
sempre; parcial, ele só vale enquanto a reserva está viva — cancelada, o lugar
volta a ser reservável. A violação é traduzida pelo adaptador de persistência
em `ReservaDomainException`, que o controller devolve como `409 Conflict`.

---

## 5. Banco — migration V6

`V6__reservas_event_driven.sql` recria a tabela `reservas` com **identidade
UUID e sem chave estrangeira para o módulo Catálogo**.

A V1 havia criado `reservas.sessao_id` referenciando `sessoes(id)`, o que
acopla dois bounded contexts no nível do banco e contraria o isolamento de
dados que a arquitetura exige. A partir da V6 a referência é um UUID lógico,
igual ao que a V5 já havia feito em `pagamentos`. Também remove `ingressos`,
que tinha FK para `reservas`.

> **Pendência para a Dupla 1:** as tabelas do Catálogo (`filmes`, `salas`,
> `assentos`, `sessoes`) continuam em `BIGSERIAL` e precisarão ser alinhadas
> para UUID. O módulo Reservas não depende mais delas — a integração é só por
> evento.

---

## 6. API

| Método | Rota | Efeito |
|---|---|---|
| `POST` | `/api/v1/reservas` | Abre a reserva e publica `AssentoReservadoEvent` |
| `GET` | `/api/v1/reservas/{id}` | Consulta o estado atual |

---

## 7. Testes

`CicloEventDrivenReservaTest` — 5 casos cobrindo o ciclo completo:
confirmação, recusa, expiração, reentrega idempotente de evento e tentativa de
reserva duplicada.

Build verde com 38 testes no total do projeto, incluindo as 3 provas de
fiscalização arquitetural (Spring Modulith + ArchUnit).
