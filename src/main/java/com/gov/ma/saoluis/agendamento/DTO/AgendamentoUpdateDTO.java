package com.gov.ma.saoluis.agendamento.DTO;

public record AgendamentoUpdateDTO(
        String nomeCidadao,
        Long servicoId,
        Long tipoAtendimentoId,
        String observacao
) {}
