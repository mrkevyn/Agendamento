package com.gov.ma.saoluis.agendamento.DTO;

public record AgendamentoUpdateResponseDTO(
        Long id,
        String nomeCidadao,
        Long servicoId,
        String servicoNome,
        Long enderecoId,
        String senha,
        String situacao,
        String tipoAtendimento
) {}
