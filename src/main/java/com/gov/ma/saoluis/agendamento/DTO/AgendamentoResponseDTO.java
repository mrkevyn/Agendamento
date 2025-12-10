package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
        Long agendamentoId,
        LocalDateTime horaAgendamento,
        String situacao,
        String senha,
        String tipoAtendimento,
        Long usuarioId,
        String usuarioNome,
        Long servicoId,
        String servicoNome
) {}
