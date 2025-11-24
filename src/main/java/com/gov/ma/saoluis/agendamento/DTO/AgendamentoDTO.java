package com.gov.ma.saoluis.agendamento.DTO;

import java.sql.Date;
import java.sql.Timestamp;

public record AgendamentoDTO(
    Long agendamentoId,
    Timestamp horaAgendamento,
    String situacao,
    String senha,
    String tipoAtendimento,
    Long usuarioId,
    String usuarioNome,
    Date dataNascimento,
    Long servicoId,
    String servicoNome
) {}
