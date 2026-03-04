package com.gov.ma.saoluis.agendamento.DTO;

public record AgendamentoEspontaneoDTO(
        String nomeCidadao,
        Long tipoAtendimentoId,
        Long servicoId, // Deve bater com o JSON do Vue
        Long setorId,   // Deve bater com o JSON do Vue
        String situacao
) {}