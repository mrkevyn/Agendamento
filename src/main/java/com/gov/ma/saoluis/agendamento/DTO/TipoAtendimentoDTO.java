package com.gov.ma.saoluis.agendamento.DTO;

public record TipoAtendimentoDTO(
        Long id,          // 🟢 Adicione o ID aqui
        Boolean ativo,
        String nome,
        Long secretariaId,
        Integer peso
) {}