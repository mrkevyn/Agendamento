package com.gov.ma.saoluis.agendamento.DTO;

import java.util.List;

public record LoginResponseDTO(
        Long id,
        String nome,
        String perfil,
        String token,
        List<SecretariaDTO> secretarias,
        List<SetorDTO> setores // 👈 Fundamental para o filtro no Vue
) {}