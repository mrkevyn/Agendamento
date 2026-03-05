package com.gov.ma.saoluis.agendamento.DTO;

public record TipoAtendimentoDTO(
        String nome,
        Boolean ativo,
        Long secretariaId,
        Integer peso
) {}