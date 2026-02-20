package com.gov.ma.saoluis.agendamento.DTO;

public record SetorDTO(
        Long id,
        String nome,
        Long secretariaId // 🔴 Adicione este campo para o vínculo
) {}