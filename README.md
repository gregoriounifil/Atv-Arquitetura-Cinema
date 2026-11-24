<div align="center">

# Sistema de Cinema — Event-Driven, Hexagonal & Monólito Modular

<p>
  <strong>Backend de gerenciamento de cinema em Monólito Modular, com Arquitetura Hexagonal e comunicação Orientada a Eventos (Event-Driven)</strong>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-21_(Virtual_Threads)-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 4.1.2" />
  <img src="https://img.shields.io/badge/Spring_Modulith-Outbox-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Modulith" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 16" />
  <img src="https://img.shields.io/badge/RabbitMQ-3.13-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="RabbitMQ 3.13" />
</p>

<p>
  <sub>Repositório: <a href="https://github.com/gregoriounifil/Atv-Arquitetura-Cinema"><code>gregoriounifil/Atv-Arquitetura-Cinema</code></a> · Conferido contra a branch <code>main</code> em 23/11/2026</sub>
</p>

</div>

---

## Sumário

- [1. Introdução](#1-introdução)
- [2. Visão Geral da Arquitetura e Versionamento](#2-visão-geral-da-arquitetura-e-versionamento)
- [3. Requisitos Funcionais e Regras de Negócio](#3-requisitos-funcionais-e-regras-de-negócio)
- [4. Requisitos Não Funcionais](#4-requisitos-não-funcionais)
- [5. Diagramação UML](#5-diagramação-uml)

---

## 1. Introdução

O repositório `Atv-Arquitetura-Cinema` reúne o backend de um sistema de gerenciamento de cinema, desenvolvido de forma modular por duplas para a atividade da disciplina de **Arquitetura de Software**. O sistema cobre o cadastro e a consulta do **catálogo** (filmes, salas e sessões), a **reserva de assentos** e o **processamento de pagamentos** de ingressos via cartão e PIX.

A aplicação estrutura-se como um **monólito modular** em Java, organizado com **Spring Modulith**. Cada módulo segue a **arquitetura hexagonal** para isolar as regras de negócio, e a comunicação entre os módulos ocorre exclusivamente por **eventos**, evitando acoplamento direto.

> **Métricas da Codebase (`main` — 23/11/2026):** **117 arquivos Java** (Catálogo: `36`, Reservas: `16`, Pagamentos: `57`, Shared: `8`) e **79 testes automatizados**.

---

## 2. Visão Geral da Arquitetura e Versionamento

O módulo de **Pagamentos** estabelece o padrão arquitetural adotado pelos demais módulos do sistema, combinando três estilos complementares:

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
      <td>Cada módulo declara fronteiras explícitas por meio da anotação <code>@ApplicationModule</code>, restringindo suas dependências apenas aos pacotes <code>shared</code> e <code>shared::events</code>.</td>
    </tr>
    <tr>
      <td><strong>Arquitetura Hexagonal (Portas e Adaptadores)</strong></td>
      <td>O núcleo de domínio permanece em Java puro, isolado de frameworks, comunicando-se com o exterior por portas de entrada e de saída implementadas por adaptadores web (REST), persistência (JPA), mensageria e gateways.</td>
    </tr>
    <tr>
      <td><strong>Arquitetura Orientada a Eventos (Event-Driven)</strong></td>
      <td>Os contratos de integração residem em <code>shared/events</code>, impedindo chamadas diretas entre módulos. A publicação dos eventos utiliza o padrão <strong>outbox transacional</strong> do Spring Modulith (tabela <code>event_publication</code>).</td>
    </tr>
  </tbody>
</table>

> **Governança Arquitetural:** A classe `ArchitectureGuardTest` valida essas fronteiras automaticamente em tempo de build, executando a verificação do Spring Modulith (`verify()`) e **três regras do ArchUnit**.

### 2.1 Tecnologias e Infraestrutura

- **Linguagem & Runtime:** Java 21 (com **Virtual Threads** habilitadas via `spring.threads.virtual.enabled`)
- **Framework & Build:** Spring Boot `4.1.2`, Spring Modulith, Maven (via Maven Wrapper `mvnw`), JPA com MapStruct
- **Banco de Dados & Mensageria:** PostgreSQL 16 e RabbitMQ 3.13 orquestrados via Docker Compose, Flyway (`ddl-auto: validate`)
- **Web, Documentação & Padrões:** Thymeleaf, OpenAPI com Swagger UI e tratamento de erros padronizado pela **RFC 7807 (Problem Details)**

> **ℹNota sobre o RabbitMQ:** O broker está provisionado no arquivo `compose.yaml` e configurado no `application.yml`, mas nenhum evento utiliza a anotação `@Externalized` até o momento. Dessa forma, os eventos trafegam em processo pelo outbox do Spring Modulith, deixando a infraestrutura pronta para externalização futura.

### 2.2 Organização dos Módulos

<table>
  <thead>
    <tr>
      <th>Módulo</th>
      <th>Responsabilidade</th>
      <th>Elementos Principais (Branch <code>main</code>)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Catálogo</strong></td>
      <td>Cadastro de filmes e salas; programação de sessões sem sobreposição na mesma sala.</td>
      <td>Entidades <code>Filme</code>, <code>Sala</code>, <code>Sessao</code>, <code>ClassificacaoIndicativa</code>, controllers REST, persistência JPA e publicação de <code>SessaoCriadaEvent</code>.</td>
    </tr>
    <tr>
      <td><strong>Reservas</strong></td>
      <td>Reserva de assentos orientada a eventos e controle de expiração.</td>
      <td>Agregado <code>Reserva</code>; portas de entrada <code>ReservarAssentoUseCase</code> e <code>DesfechoPagamentoUseCase</code>; portas de saída <code>ReservaRepositoryPort</code> e <code>PublicarEventoReservaPort</code>.</td>
    </tr>
    <tr>
      <td><strong>Pagamentos</strong></td>
      <td>Cobrança, processamento (cartão e PIX) e expiração de prazos.</td>
      <td>Entidade <code>Pagamento</code>, <code>DadosCartao</code>, <code>StatusPagamento</code>, portas <code>GatewayCartaoPort</code> e <code>GatewayPixPort</code>, gateway simulado (fake), agendador de expiração, checkout e sandbox.</td>
    </tr>
    <tr>
      <td><strong>Shared</strong></td>
      <td>Contratos de eventos de integração e configurações transversais.</td>
      <td>Pacote <code>shared/events</code> (contendo 5 eventos) e configuração OpenAPI.</td>
    </tr>
  </tbody>
</table>

### 2.3 Versionamento de Código e Esquema de Banco de Dados

O versionamento de código no Git seguiu o fluxo de Pull Requests por módulo (**PR #1** para a base de pagamentos, **PR #2** para catálogo e reservas, e **PR #3** para o fluxo completo hexagonal de pagamentos com sandbox).

<details open>
<summary><strong>Migrations do Banco de Dados PostgreSQL (Flyway · <code>ddl-auto: validate</code>)</strong></summary>
<br />
<ul>
  <li><code>V1</code> a <code>V3</code> — Criação das tabelas iniciais e da estrutura de outbox transacional (<code>event_publication</code>).</li>
  <li><code>V4</code> — Estruturação da tabela de usuários.</li>
  <li><code>V5</code> — Criação das tabelas do módulo de pagamentos.</li>
  <li><code>V6</code> — Criação das tabelas de reservas utilizando chaves primárias <code>UUID</code> e <strong>índice único parcial</strong> para assentos ativos.</li>
  <li><code>V7</code> — Criação das tabelas de filmes e salas com chaves <code>UUID</code>.</li>
  <li><code>V8</code> — Criação da tabela de sessões com restrição de exclusão (<code>EXCLUDE USING gist</code>) contra sobreposição de horários.</li>
</ul>
</details>

---

## 3. Requisitos Funcionais e Regras de Negócio

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Requisito Funcional</th>
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
      <td>Programar sessões de exibição, sem sobreposição na mesma sala</td>
      <td>Catálogo</td>
      <td>PR #2 (<code>V8</code>)</td>
    </tr>
    <tr>
      <td><strong>RF04</strong></td>
      <td>Consultar filmes, salas e sessões</td>
      <td>Catálogo</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF05</strong></td>
      <td>Publicar o evento <code>SessaoCriadaEvent</code> ao programar uma sessão</td>
      <td>Catálogo</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF06</strong></td>
      <td>Reservar um assento</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF07</strong></td>
      <td>Impedir duas reservas ativas para o mesmo assento</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF08</strong></td>
      <td>Publicar o evento <code>AssentoReservadoEvent</code> após a reserva</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF09</strong></td>
      <td>Atualizar a reserva conforme o resultado do pagamento (<code>PagamentoAprovado</code>, <code>PagamentoRecusado</code> ou <code>PagamentoExpirado</code>), com transições idempotentes</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF10</strong></td>
      <td>Consultar uma reserva</td>
      <td>Reservas</td>
      <td>PR #2</td>
    </tr>
    <tr>
      <td><strong>RF11</strong></td>
      <td>Abrir a cobrança ao consumir o <code>AssentoReservadoEvent</code></td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF12</strong></td>
      <td>Processar pagamento com cartão de crédito ou débito</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF13</strong></td>
      <td>Processar pagamento com PIX</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF14</strong></td>
      <td>Consultar um pagamento e seu status, por identificador ou por reserva</td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF15</strong></td>
      <td>Expirar o pagamento após 10 minutos sem pagar e cancelar a reserva automaticamente</td>
      <td>Pagamentos e Reservas</td>
      <td>PR #3</td>
    </tr>
    <tr>
      <td><strong>RF16</strong></td>
      <td>Publicar <code>PagamentoAprovado</code>, <code>PagamentoRecusado</code> ou <code>PagamentoExpirado</code></td>
      <td>Pagamentos</td>
      <td>PR #1</td>
    </tr>
    <tr>
      <td><strong>RF17</strong></td>
      <td>Simular aprovação, recusa e expiração em uma tela de sandbox no navegador</td>
      <td>Pagamentos</td>
      <td>PR #3</td>
    </tr>
  </tbody>
</table>

### 3.1 Regras de Negócio

- **RN01:** Toda reserva que permanecer sem pagamento por **10 minutos** é cancelada automaticamente.
- **RN02:** Um assento não pode possuir duas reservas ativas simultaneamente; o banco de dados garante essa restrição por meio de um **índice único parcial**.
- **RN03:** Como eventos podem ser reentregues pelo barramento, as transições de estado da reserva são **idempotentes**.
- **RN04:** O módulo de pagamentos aceita cartão (crédito ou débito) e PIX, utilizando **portas de saída segregadas** para cada meio.
- **RN05:** Duas sessões não podem ocorrer em horários sobrepostos na mesma sala; a restrição `EXCLUDE USING gist` (`V8`) assegura essa regra no banco mesmo sob requisições concorrentes.
- **RN06:** Um pagamento recusado cancela a reserva imediatamente e libera o assento, não permitindo nova tentativa sobre a mesma reserva.
- **RN07:** Dados sensíveis de cartão não são gravados em banco; o objeto `DadosCartao` existe apenas em memória durante o processamento.

---

## 4. Requisitos Não Funcionais

Os requisitos não funcionais seguem as categorias do modelo de qualidade **ISO/IEC 25010**.

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Categoria (ISO/IEC 25010)</th>
      <th>Requisito</th>
      <th>Evidência no Código</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>RNF01</strong></td>
      <td>Manutenibilidade (Modularidade)</td>
      <td>Módulos com fronteiras explícitas, dependendo apenas de <code>shared</code> e <code>shared::events</code></td>
      <td><code>@ApplicationModule</code> e <code>verify()</code> do Spring Modulith</td>
    </tr>
    <tr>
      <td><strong>RNF02</strong></td>
      <td>Manutenibilidade (Testabilidade)</td>
      <td>Domínio em Java puro, isolado de frameworks e infraestrutura</td>
      <td>Arquitetura hexagonal e 3 regras ArchUnit</td>
    </tr>
    <tr>
      <td><strong>RNF03</strong></td>
      <td>Manutenibilidade (Baixo Acoplamento)</td>
      <td>Módulos comunicam-se apenas por eventos de integração, sem chamadas diretas</td>
      <td>Contratos em <code>shared/events</code></td>
    </tr>
    <tr>
      <td><strong>RNF04</strong></td>
      <td>Confiabilidade</td>
      <td>Publicação consistente de eventos e tolerância a reentregas</td>
      <td>Outbox transacional e transições idempotentes</td>
    </tr>
    <tr>
      <td><strong>RNF05</strong></td>
      <td>Confiabilidade (Integridade)</td>
      <td>Impedir reservas duplicadas, sessões sobrepostas e acoplamento entre contextos no banco</td>
      <td>Índice único parcial (<code>V6</code>), restrição <code>EXCLUDE</code> (<code>V8</code>), UUIDs e ausência de chaves estrangeiras entre módulos</td>
    </tr>
    <tr>
      <td><strong>RNF06</strong></td>
      <td>Portabilidade</td>
      <td>Ambiente reproduzível com contêineres e build padronizado pelo Maven Wrapper</td>
      <td><code>compose.yaml</code> com PostgreSQL (porta <code>5433</code>) e RabbitMQ (<code>5672</code>), além de <code>mvnw</code></td>
    </tr>
    <tr>
      <td><strong>RNF07</strong></td>
      <td>Manutenibilidade (Modificabilidade)</td>
      <td>Esquema de banco versionado e reaplicável em qualquer ambiente</td>
      <td>Migrations Flyway <code>V1</code> a <code>V8</code> e Hibernate em <code>ddl-auto: validate</code></td>
    </tr>
    <tr>
      <td><strong>RNF08</strong></td>
      <td>Compatibilidade (Interoperabilidade)</td>
      <td>API REST com respostas de erro padronizadas</td>
      <td>Padrão RFC 7807 (Problem Details)</td>
    </tr>
    <tr>
      <td><strong>RNF09</strong></td>
      <td>Manutenibilidade (Analisabilidade)</td>
      <td>API documentada e explorável</td>
      <td>OpenAPI, Swagger UI e especificações em <code>docs/</code></td>
    </tr>
    <tr>
      <td><strong>RNF10</strong></td>
      <td>Manutenibilidade (Testabilidade)</td>
      <td>Cobertura com testes de domínio, serviço, fatia web, persistência e integração</td>
      <td>79 testes no projeto, incluindo 5 casos de integração do ciclo reserva-pagamento</td>
    </tr>
    <tr>
      <td><strong>RNF11</strong></td>
      <td>Manutenibilidade (Substituibilidade)</td>
      <td>Trocar o provedor de pagamento sem alterar o núcleo de domínio</td>
      <td>Gateway simulado atrás de portas segregadas para cartão e PIX</td>
    </tr>
    <tr>
      <td><strong>RNF12</strong></td>
      <td>Eficiência de Desempenho</td>
      <td>Atender picos de concorrência na reserva e compra de assentos</td>
      <td>Virtual Threads do Java 21 habilitadas (<code>spring.threads.virtual.enabled</code>)</td>
    </tr>
    <tr>
      <td><strong>RNF13</strong></td>
      <td>Segurança (Confidencialidade)</td>
      <td>Não armazenar dados sensíveis de cartão de crédito ou débito</td>
      <td>Ausência de colunas de cartão na tabela <code>pagamentos</code>; <code>DadosCartao</code> mantido apenas em memória</td>
    </tr>
  </tbody>
</table>

---

## 5. Diagramação UML

> *Os diagramas modelados representam a estrutura real extraída da branch `main`, cobrindo a visão funcional e comportamental, além dos diagramas estruturais exigidos pela atividade (pacotes, componentes e implantação).*

### 5.1 Diagrama de Caso de Uso
Atores: Cliente, Gestor do catálogo (inferido), Gateway de pagamento (simulado) e Agendador de expiração (job interno).

<p align="center">
  <img src="docs\Diagramas\diagrama caso de uso.png" alt="Diagrama de Caso de Uso" width="850" />
</p>

### 5.2 Diagrama de Componentes
Módulos do monólito, módulo compartilhado de eventos e banco de dados, com os eventos que ligam os módulos.

<p align="center">
  <img src="docs\Diagramas\diagrama componentes.png" alt="Diagrama de Componentes" width="850" />
</p>

### 5.3 Arquitetura Hexagonal do Módulo Pagamentos
Portas e adaptadores do módulo, em diagrama de componentes.

<p align="center">
  <img src="docs\Diagramas\diagrama arquitetura hexagonal.png" alt="Arquitetura Hexagonal do Módulo Pagamentos" width="850" />
</p>

### 5.4 Diagrama de Pacotes
Estrutura dos pacotes dos módulos com fronteiras `@ApplicationModule` e camadas hexagonais.

<p align="center">
  <img src="docs/Diagramas/diagrama pacotes.jpeg" alt="Diagrama de Pacotes" width="850" />
</p>
<p align="center">
  <img src="docs\Diagramas\diagrama pacotes 2.jpeg" alt="Diagrama de Pacotes 2" width="850" />
</p>

### 5.5 Diagrama de Implantação
Topologia de execução da aplicação Java (`mvnw`) conectada ao contêiner PostgreSQL e acessada via navegador (Swagger UI e sandbox).

<p align="center">
  <img src="docs\Diagramas\diagrama implantacao.jpg" alt="Diagrama de Implantação" width="850" />
</p>