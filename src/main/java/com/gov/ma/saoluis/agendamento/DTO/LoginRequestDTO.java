package com.gov.ma.saoluis.agendamento.DTO;

public record LoginRequestDTO(
        String login, // cpf ou email
        String senha
) {}
