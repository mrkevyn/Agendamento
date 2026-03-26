package com.gov.ma.saoluis.agendamento.DTO;

public record AgendamentoUpdateResponseDTO(
        Long id,
        String nomeCidadao,
        Long servicoId,
        String servicoNome,
        Long setorId,
        String senha,
        String situacao,
        String tipoAtendimento,
        String observacao
) {}
