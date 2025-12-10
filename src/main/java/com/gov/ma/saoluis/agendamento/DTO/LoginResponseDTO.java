package com.gov.ma.saoluis.agendamento.DTO;

public record LoginResponseDTO(
        Long id,
        String nome,
        String acesso,
        Long secretaria,
        Integer guiche
) {}
