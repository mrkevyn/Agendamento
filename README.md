# 📅 Sistema de Agendamento de Atendimento

Este projeto é um sistema backend para gerenciamento de agendamentos de atendimento em órgãos públicos, permitindo controle de filas, geração de senhas, chamadas e acompanhamento do atendimento em tempo real.

---

## 🚀 Funcionalidades

* 📌 Criação de agendamentos (com horário)
* ⚡ Atendimento espontâneo (sem agendamento prévio)
* 🔢 Geração automática de senhas (Normal, Prioridade, Preferencial)
* 📋 Listagem de agendamentos com detalhes
* 🔄 Reagendamento
* 📢 Chamada de próxima senha:

  * Normal
  * Prioridade
  * Por número específico
* 🧾 Registro de histórico de chamadas
* ✅ Finalização de atendimento
* ❌ Cancelamento (não comparecimento)

---

## 🧠 Regras de Negócio (Destaque)

### 🔐 Controle de Horários

* Um horário só pode ser utilizado uma vez
* Ao agendar, o sistema bloqueia automaticamente o horário

### 🎟️ Geração de Senhas

As senhas são geradas automaticamente com base no tipo de atendimento:

| Tipo         | Prefixo | Exemplo |
| ------------ | ------- | ------- |
| Normal       | N       | N001    |
| Prioridade   | P       | P001    |
| Preferencial | F       | F001    |

* A numeração é reiniciada diariamente por secretaria
* Incremento automático baseado no total de atendimentos do dia

---

### 🔥 Atendimento Espontâneo

* Não depende de horário
* Gera senha automaticamente
* Valida:

  * Serviço
  * Configuração ativa
  * Nome do cidadão

---

### 📢 Sistema de Chamada

* O sistema chama automaticamente o próximo da fila
* Atualiza status para **EM_ATENDIMENTO**
* Registra:

  * Atendente
  * Guichê
  * Horário da chamada
* Salva histórico na entidade `ChamadaAgendamento`

---

### 🔄 Estados do Agendamento

* `AGENDADO`
* `EM_ATENDIMENTO`
* `ATENDIDO`
* `REAGENDADO`
* `FALTOU`

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura baseada em camadas:

* **Controller** → Entrada das requisições
* **Service** → Regras de negócio (**camada principal**)
* **Repository** → Acesso ao banco de dados
* **DTOs** → Comunicação com cliente

---

## 🧩 Service Principal

A classe `AgendamentoService` centraliza toda a lógica do sistema:

Principais responsabilidades:

* Gerenciar criação de agendamentos
* Controlar geração de senhas
* Validar regras de negócio
* Gerenciar fila de atendimento
* Registrar chamadas
* Controlar estados do atendimento

---

## 🛠️ Tecnologias Utilizadas

* Java
* Spring Boot
* Spring Data JPA
* Banco de dados relacional
* API REST

---

## 📡 Exemplos de Fluxo

### 1. Criar Agendamento

1. Usuário escolhe serviço e horário
2. Sistema valida disponibilidade
3. Gera senha automaticamente
4. Salva como `AGENDADO`

---

### 2. Chamada de Atendimento

1. Atendente solicita próxima senha
2. Sistema busca próxima disponível
3. Atualiza para `EM_ATENDIMENTO`
4. Registra histórico da chamada

---

### 3. Finalização

* Atendimento → `ATENDIDO`
* Não compareceu → `FALTOU`

---

## 📂 Estrutura (resumo)

```
service/
 └── AgendamentoService.java

repository/
 └── AgendamentoRepository.java

model/
 └── Agendamento.java

DTO/
 └── AgendamentoDTO.java
```

---

## 💡 Diferenciais do Projeto

* Regras reais de fila de atendimento
* Controle de concorrência em horários
* Geração inteligente de senhas
* Histórico completo de chamadas
* Separação clara de responsabilidades (boa arquitetura)

---

## 👨‍💻 Autor

Desenvolvido como projeto de portfólio focado em backend, com ênfase em regras de negócio complexas e sistemas reais de atendimento.

---
