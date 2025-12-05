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

    Long getUsuarioId();
    String getUsuarioNome();

    Long getServicoId();
    String getServicoNome();
}
