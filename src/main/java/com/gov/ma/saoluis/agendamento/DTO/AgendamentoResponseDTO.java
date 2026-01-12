package com.gov.ma.saoluis.agendamento.DTO;

import com.gov.ma.saoluis.agendamento.model.SituacaoAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
        Long agendamentoId,
        LocalDateTime horaAgendamento,
        SituacaoAgendamento situacao,
        String senha,
        String tipoAtendimento,
        Long usuarioId,
        String usuarioNome,
        Long servicoId,
        String servicoNome
) {}
