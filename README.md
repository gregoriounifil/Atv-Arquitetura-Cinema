<div align="center">

# 🎬 Cinema — Event-Driven, Hexagonal & Monólito Modular

<p>
  <strong>Sistema de cinema em monólito modular, com arquitetura hexagonal e comunicação orientada a eventos (Event-Driven)</strong>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Spring_Modulith-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Modulith" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Arquitetura-Hexagonal_%7C_Event--Driven-blue?style=for-the-badge" alt="Arquitetura" />
</p>

<p>
  <sub>Repositório: <code>gregoriounifil/Atv-Arquitetura-Cinema</code>
</p>

</div>

---

## 📑 Sumário

- [1. Introdução](#1-introdução)
- [2. Visão Geral da Arquitetura](#2-visão-geral-da-arquitetura)
- [3. Requisitos Funcionais](#3-requisitos-funcionais)
- [4. Requisitos Não Funcionais](#4-requisitos-não-funcionais)
- [5. Diagramas UML](#5-diagramas-uml)

---

## 1. Introdução

O repositório `Atv-Arquitetura-Cinema` reúne o backend de um sistema de cinema, desenvolvido em módulos por duplas como atividade de arquitetura de software. O sistema cobre o **catálogo** (filmes e salas), a **reserva de assentos** e o **pagamento de ingressos** com cartão e PIX.

O projeto tem como foco a combinação de três estilos arquiteturais complementares:
- **Monólito Modular:** organizado com Spring Modulith.
- **Arquitetura Hexagonal:** isolamento do domínio por portas e adaptadores.
- **Arquitetura Orientada a Eventos (Event-Driven):** garante que os módulos se comuniquem exclusivamente por eventos, sem acoplamento direto.

---

## 2. Visão Geral da Arquitetura

O módulo **Pagamentos** estabelece o padrão arquitetural que os demais módulos seguem, combinando três estilos:

<table>
  <thead>
    <tr>
      <th>Estilo Arquitetural</th>
      <th>Aplicação no Projeto</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Monólito Modular</strong></td>
      <td>Cada módulo tem fronteiras declaradas com <code>@ApplicationModule</code>, e as dependências ficam restritas a <code>shared</code> e <code>shared::events</code>.</td>
    </tr>
    <tr>
      <td><strong>Hexagonal (Portas e Adaptadores)</strong></td>
      <td>Domínio puro, portas de entrada e de saída, e adaptadores web (REST), JPA, mensageria e gateway.</td>
    </tr>
    <tr>
      <td><strong>Orientada a Eventos</strong></td>
      <td>Os contratos ficam em <code>shared/events</code> e os módulos não se chamam diretamente. A publicação dos eventos de pagamento usa <strong>outbox transacional</strong>.</td>
    </tr>
  </tbody>
</table>

> **🛡️ Governança Arquitetural:** O `ArchitectureGuardTest` verifica essas regras automaticamente, com o `verify()` do Spring Modulith e **3 regras do ArchUnit**.

### 🛠️ Stack Tecnológica

- **Linguagem & Build:** Java, Maven (Maven Wrapper)
- **Framework & Arquitetura:** Spring Modulith, JPA com MapStruct
- **Banco de Dados & Migrations:** PostgreSQL (Docker Compose, porta `5433`), Flyway
- **Documentação & Padrões de API:** OpenAPI com Swagger UI e respostas de erro no padrão RFC 7807

### 📦 Módulos do Sistema

<table>
  <thead>
    <tr>
      <th>Módulo</th>
      <th>Responsabilidade</th>
      <th>Elementos Citados nos PRs</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Catálogo</strong></td>
      <td>Cadastro de filmes e salas; sessões previstas para a segunda parte (migration <code>V8</code>)</td>
      <td><code>Filme</code>, <code>Sala</code>, <code>ClassificacaoIndicativa</code>, controllers REST, persistência JPA</td>
    </tr>
    <tr>
      <td><strong>Reservas</strong></td>
      <td>Reserva de assentos, orientada a eventos</td>
      <td>Agregado <code>Reserva</code>, <code>AssentoReservadoEvent</code>, 2 portas de entrada e 2 de saída</td>
    </tr>
    <tr>
      <td><strong>Pagamentos</strong></td>
      <td>Cobrança, processamento (cartão e PIX) e expiração</td>
      <td><code>Pagamento</code>, <code>DadosCartao</code>, <code>StatusPagamento</code>, gateway fake, job de expiração, sandbox</td>
    </tr>
    <tr>
      <td><strong>Shared</strong></td>
      <td>Contratos de eventos e configuração transversal</td>
      <td><code>shared/events</code>, configuração OpenAPI</td>
    </tr>
  </tbody>
</table>

<details>
<summary><strong>🗄️ Versionamento do Banco de Dados (Flyway)</strong></summary>
<br />
<ul>
  <li><code>V4</code> — Usuários</li>
  <li><code>V5</code> — Pagamentos</li>
  <li><code>V6</code> — Reservas (com identidade <code>UUID</code>)</li>
  <li><code>V7</code> — Filmes e salas (com identidade <code>UUID</code>)</li>
  <li><code>V8</code> — Sessões <em>(prevista)</em></li>
</ul>
</details>

---

## 3. Requisitos Funcionais

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Requisito</th>
      <th>Módulo</th>
      <th>Origem</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>RF01</strong></td>
      <td>Cadastrar filmes, informando a classificação indicativa</td>
      <td>Catálogo</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF02</strong></td>
      <td>Cadastrar salas</td>
      <td>Catálogo</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF03</strong></td>
      <td>Cadastrar sessões de exibição</td>
      <td>Catálogo</td>
      <td>Planejado (PR #2, migration <code>V8</code>)</td>
    </tr>
    <tr>
      <td><strong>RF04</strong></td>
      <td>Reservar um assento</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF05</strong></td>
      <td>Impedir duas reservas ativas para o mesmo assento</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF06</strong></td>
      <td>Publicar o evento <code>AssentoReservadoEvent</code> após a reserva</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF07</strong></td>
      <td>Atualizar a reserva conforme o resultado do pagamento (<code>PagamentoAprovado</code>, <code>PagamentoRecusado</code> ou <code>PagamentoExpirado</code>), com transições idempotentes</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF08</strong></td>
      <td>Consultar reservas</td>
      <td>Reservas</td>
      <td>Inferido</td>
    </tr>
    <tr>
      <td><strong>RF09</strong></td>
      <td>Abrir a cobrança ao consumir o <code>AssentoReservadoEvent</code></td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF10</strong></td>
      <td>Processar pagamento com cartão de crédito ou débito</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF11</strong></td>
      <td>Processar pagamento com PIX</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF12</strong></td>
      <td>Consultar um pagamento e seu status</td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF13</strong></td>
      <td>Expirar o pagamento após 10 minutos sem pagar e cancelar a reserva automaticamente</td>
      <td>Pagamentos e Reservas</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF14</strong></td>
      <td>Publicar <code>PagamentoAprovado</code>, <code>PagamentoRecusado</code> ou <code>PagamentoExpirado</code></td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF15</strong></td>
      <td>Simular aprovação, recusa e expiração em uma tela de sandbox no navegador</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
  </tbody>
</table>

### 3.1 Regras de Negócio

- **RN01:** A reserva sem pagamento em 10 minutos é cancelada automaticamente.
- **RN02:** Um assento não pode ter duas reservas ativas ao mesmo tempo; o banco reforça a regra com um índice único parcial.
- **RN03:** Eventos podem ser reentregues, então as transições de estado da reserva são estáveis a múltiplas execuções (idempotentes).
- **RN04:** O pagamento aceita cartão (crédito ou débito) e PIX, com portas separadas para cada meio.

---

## 4. Requisitos Não Funcionais

As categorias seguem o modelo de qualidade da **ISO/IEC 25010**.

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Categoria</th>
      <th>Requisito</th>
      <th>Evidência nos PRs</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>RNF01</strong></td>
      <td>Manutenibilidade (modularidade)</td>
      <td>Módulos com fronteiras explícitas, dependendo apenas de <code>shared</code> e <code>shared::events</code></td>
      <td><code>@ApplicationModule</code> e <code>verify</code> do Spring Modulith</td>
    </tr>
    <tr>
      <td><strong>RNF02</strong></td>
      <td>Manutenibilidade (testabilidade)</td>
      <td>Domínio em Java puro, isolado de framework e infraestrutura</td>
      <td>Arquitetura hexagonal e 3 regras ArchUnit</td>
    </tr>
    <tr>
      <td><strong>RNF03</strong></td>
      <td>Manutenibilidade (baixo acoplamento)</td>
      <td>Módulos se comunicam apenas por eventos de integração, sem chamadas diretas</td>
      <td>Contratos em <code>shared/events</code></td>
    </tr>
    <tr>
      <td><strong>RNF04</strong></td>
      <td>Confiabilidade</td>
      <td>Publicação consistente de eventos e tolerância a reentrega</td>
      <td>Outbox transacional e transições idempotentes</td>
    </tr>
    <tr>
      <td><strong>RNF05</strong></td>
      <td>Confiabilidade (integridade dos dados)</td>
      <td>Impedir reservas duplicadas e acoplamento entre contextos no banco</td>
      <td>Índice único parcial, identidades UUID e remoção da chave estrangeira entre contextos (<code>V6</code>)</td>
    </tr>
    <tr>
      <td><strong>RNF06</strong></td>
      <td>Portabilidade</td>
      <td>Ambiente reproduzível com contêiner e build pelo Maven Wrapper</td>
      <td><code>compose.yaml</code> com PostgreSQL (porta <code>5433</code>) e <code>mvnw</code></td>
    </tr>
    <tr>
      <td><strong>RNF07</strong></td>
      <td>Manutenibilidade (modificabilidade)</td>
      <td>Esquema do banco versionado e reaplicável em qualquer ambiente</td>
      <td>Migrations Flyway (<code>V1</code> a <code>V7</code>; <code>V8</code> prevista)</td>
    </tr>
    <tr>
      <td><strong>RNF08</strong></td>
      <td>Compatibilidade (interoperabilidade)</td>
      <td>API REST com respostas de erro padronizadas</td>
      <td>RFC 7807 (Problem Details)</td>
    </tr>
    <tr>
      <td><strong>RNF09</strong></td>
      <td>Manutenibilidade (analisabilidade)</td>
      <td>API documentada e explorável</td>
      <td>OpenAPI, Swagger UI e <code>docs/MODULO_RESERVAS.md</code></td>
    </tr>
    <tr>
      <td><strong>RNF10</strong></td>
      <td>Manutenibilidade (testabilidade)</td>
      <td>Testes de domínio, de fatia web e de integração do ciclo completo</td>
      <td>38 testes no build do módulo Reservas, com 5 casos de integração do ciclo reserva e pagamento</td>
    </tr>
    <tr>
      <td><strong>RNF11</strong></td>
      <td>Manutenibilidade (substituibilidade)</td>
      <td>Trocar o provedor de pagamento sem alterar o domínio</td>
      <td>Gateway fake atrás de portas; portas de cartão e PIX segregadas</td>
    </tr>
  </tbody>
</table>

---

## 5. Diagramas UML

> *Os diagramas foram modelados a partir dos nomes de classes, portas, eventos e fluxos descritos nos PRs. Atributos, métodos e transições detalhadas são ilustrativos.*

- **5.1 Diagrama de casos de uso:** Atores: Cliente, Gestor do catálogo (inferido), Gateway de pagamento (simulado) e Agendador de expiração (job interno).
- **5.2 Diagrama de componentes:** Módulos do monólito, módulo compartilhado de eventos e banco de dados, com os eventos que ligam os módulos.
- **5.3 Diagrama de classes:** Núcleo de domínio de cada módulo. Entre módulos, as referências são feitas por identificador, nunca por associação direta.
- **5.4 Diagramas de sequência:** Fluxo principal de reserva e pagamento, com os desfechos aprovado ou recusado, e o fluxo de expiração após 10 minutos.
- **5.5 Diagramas de estados:** Ciclo de vida da reserva e do pagamento.
- **5.6 Arquitetura hexagonal do módulo Pagamentos:** Portas e adaptadores do módulo, em diagrama de componentes.
