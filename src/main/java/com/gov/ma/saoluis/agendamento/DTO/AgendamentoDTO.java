package com.gov.ma.saoluis.agendamento.DTO;

import java.sql.Date;
import java.sql.Timestamp;

import java.time.LocalDateTime;

public interface AgendamentoDTO {
    Long getAgendamentoId();
    LocalDateTime getHoraAgendamento();
    String getSituacao();
    String getSenha();
    String getTipoAtendimento();
    String getTipoAgendamento();

    Long getUsuarioId();
    String getUsuarioNome();

    Long getServicoId();
    String getServicoNome();

    // 🔹 Novos campos da secretaria
    Long getSecretariaId();
    String getSecretariaNome();

    Integer getGuiche();
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
