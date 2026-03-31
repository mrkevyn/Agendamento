package com.gov.ma.saoluis.agendamento.DTO;

public record SetorCreateDTO(
        String nome,
        String descricao,
        Long enderecoId,
        Long secretariaId
) {}