package com.gov.ma.saoluis.agendamento.DTO;

public record AgendamentoAppRequest(
        Long usuarioId,
        Long servicoId,
        Long horarioId,
        String tipoAtendimento
) {}
