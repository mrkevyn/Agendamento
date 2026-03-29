# 📅 Sistema de Agendamento de Atendimento

Sistema backend desenvolvido para gerenciamento de atendimentos em órgãos públicos, com controle de filas, geração de senhas, distribuição por guichês e acompanhamento em tempo real.

O projeto simula um ambiente real de atendimento, com regras de negócio complexas e foco em consistência e organização do fluxo.

---

## 🚀 Funcionalidades

* 📌 Criação de agendamentos com horário
* ⚡ Atendimento espontâneo (sem agendamento prévio)
* 🔢 Geração automática de senhas (Normal, Prioridade)
* 📋 Listagem de agendamentos com detalhes
* 🔄 Reagendamento de atendimentos
* 📢 Sistema de chamada:

  * Próxima senha normal
  * Próxima senha prioritária
  * Chamada por número específico
* 🧾 Registro completo de histórico de chamadas
* 🏢 Controle de atendentes e guichês
* ✅ Finalização de atendimento
* ❌ Cancelamento (não comparecimento)
* 📝 Auditoria de ações (logs)

---

## 🧠 Regras de Negócio

### 🔐 Controle de Horários

* Cada horário pode ser utilizado apenas uma vez
* O sistema bloqueia automaticamente o horário ao agendar
* Validação por:

  * Dia da semana
  * Intervalo de funcionamento
  * Configuração ativa da secretaria

---

### ⏰ Geração Inteligente de Horários

Os horários são gerados dinamicamente com base em duas estratégias:

#### ➤ Por Intervalo

Define horários com espaçamento fixo

**Exemplo:**
08:00 → 08:20 → 08:40 → ...

---

#### ➤ Por Quantidade

Distribui automaticamente os horários dentro do período

**Exemplo:**
08:00 → 08:30 → 09:00 → 09:30

---

### 🎟️ Geração de Senhas

| Tipo       | Prefixo | Exemplo |
| ---------- | ------- | ------- |
| Normal     | N       | N001    |
| Prioridade | P       | P001    |

* Numeração reiniciada diariamente por secretaria
* Incremento automático baseado no volume diário
* Independente de horário (para atendimento espontâneo)

---

### ⚡ Atendimento Espontâneo

* Não depende de horário prévio
* Geração automática de senha
* Valida:

  * Serviço
  * Configuração ativa
  * Dados do cidadão

---

### 🏢 Sistema de Guichês

* Cada guichê é único por secretaria
* Um guichê não pode ser utilizado por dois atendentes simultaneamente
* Controle de permissão:

  * **ADMIN** → gerencia todos
  * **ATENDENTE** → altera apenas o próprio guichê

---

### 📢 Sistema de Chamada

Ao chamar um atendimento:

* Status atualizado para `EM_ATENDIMENTO`
* Registro de:

  * Atendente
  * Guichê
  * Senha
  * Data e hora
* Persistência do histórico (`ChamadaAgendamento`)

---

### 🔄 Estados do Agendamento

* `AGENDADO`
* `EM_ATENDIMENTO`
* `ATENDIDO`
* `REAGENDADO`
* `FALTOU`

---

## 📡 Endpoints (Exemplo)

```http
POST   /agendamentos
GET    /agendamentos
GET    /agendamentos/{id}
PUT    /agendamentos/{id}
DELETE /agendamentos/{id}

POST   /agendamentos/chamar/normal
POST   /agendamentos/chamar/prioridade
POST   /agendamentos/chamar/{senha}

POST   /agendamentos/{id}/finalizar
POST   /agendamentos/{id}/cancelar
```

---

## 📥 Exemplo de Request

```json
POST /agendamentos

{
  "usuarioId": 1,
  "servicoId": 2,
  "horarioId": 10,
  "tipoAtendimento": "NORMAL"
}
```

---

## 📤 Exemplo de Response

```json
{
  "id": 100,
  "senha": "N001",
  "situacao": "AGENDADO",
  "tipoAtendimento": "NORMAL"
}
```

---

## 🏗️ Arquitetura

Arquitetura baseada em camadas:

* **Controller** → entrada da API
* **Service** → regras de negócio (camada central)
* **Repository** → acesso a dados
* **DTOs** → comunicação externa

---

## 🧩 Serviços Principais

### `AgendamentoService`

* Gerencia toda a lógica de atendimento
* Controle de fila e senhas
* Processamento de chamadas

### `ConfiguracaoAtendimentoService`

* Geração dinâmica de horários
* Validação de disponibilidade
* Regras de agenda

### `GerenciadorService`

* Controle de atendentes
* Gestão de guichês
* Controle de permissões

---

## 🛠️ Tecnologias Utilizadas

* Java
* Spring Boot
* Spring Data JPA
* API REST
* Banco de dados relacional

---

## 📡 Fluxos do Sistema

### ➤ Criar Agendamento

1. Usuário seleciona serviço e horário
2. Sistema valida disponibilidade
3. Horário é bloqueado
4. Senha é gerada automaticamente

---

### ➤ Chamada de Atendimento

1. Atendente solicita próxima senha
2. Sistema busca da fila
3. Atualiza status
4. Registra histórico com guichê

---

### ➤ Finalização

* Atendimento concluído → `ATENDIDO`
* Não comparecimento → `FALTOU`

---

## 💡 Diferenciais Técnicos

* Regras reais de fila de atendimento
* Geração dinâmica de horários (intervalo/quantidade)
* Controle de concorrência (horários e guichês)
* Sistema de senhas com lógica incremental
* Auditoria de ações (logs)
* Separação clara de responsabilidades (Clean Architecture)

---

## 👨‍💻 Autor

Sistema desenvolvido com foco em backend, simulando cenários reais de atendimento público, com ênfase em regras de negócio complexas, consistência de dados e organização de fluxo.
