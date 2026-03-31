package com.gov.ma.saoluis.agendamento.DTO;

public record TipoAtendimentoDTO(
        Long id,
        Boolean ativo,
        String nome,
        Long secretariaId,
        Integer peso
) {}