package com.gov.ma.saoluis.agendamento.DTO;

import java.util.List;

public record GerenciadorDTO(
        String nome,
        String cpf,
        String contato,
        String email,
        String senha,
        String perfil,      // 👈 AQUI
        Integer guiche,
        Long secretariaId,
        List<Long> secretariasIds,
        List<Long> setoresIds
) {}
