package com.gov.ma.saoluis.agendamento.DTO;

public record LoginResponseDTO(
        Long id,
        String nome,
        String perfil,
        Long secretaria,
        Integer guiche,
        String token) {}
