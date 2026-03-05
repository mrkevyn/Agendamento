package com.gov.ma.saoluis.agendamento.DTO;

import java.sql.Date;
import java.sql.Timestamp;

import java.time.LocalDateTime;

public interface AgendamentoDTO {
    Long getAgendamentoId();
    LocalDateTime getHoraAgendamento();
    String getSituacao();
    String getSenha();
    // Adicione estes métodos dentro da sua interface AgendamentoDTO
    Long getTipoAtendimentoId();
    String getTipoAtendimento(); // Esse você provavelmente já tem
    String getTipoAtendimentoSigla();
    Integer getTipoAtendimentoPeso();
    String getTipoAgendamento();

    Long getUsuarioId();
    String getUsuarioNome();

    Long getServicoId();
    String getServicoNome();

    // 🔹 Novos campos da secretaria
    Long getSecretariaId();
    String getSecretariaNome();

    String getGuiche();
    Long getGerenciadorId(); // recomendo também retornar, ajuda debug

    // 🔹 Endereço (NOVO)
    Long getEnderecoId();
    String getEnderecoLogradouro();
    String getEnderecoNumero();
    String getEnderecoBairro();
    String getEnderecoCidade();
    String getEnderecoUf();
    String getEnderecoCep();
    String getEnderecoComplemento();
}
