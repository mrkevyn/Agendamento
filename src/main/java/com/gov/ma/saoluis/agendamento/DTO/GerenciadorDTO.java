package com.gov.ma.saoluis.agendamento.DTO;

public record GerenciadorDTO(
        String nome,
        String cpf,
        String contato,
        String email,
        String senha,
        String perfil,      // 👈 AQUI
        Integer guiche,
        Long secretariaId,
        Long enderecoId
) {}
