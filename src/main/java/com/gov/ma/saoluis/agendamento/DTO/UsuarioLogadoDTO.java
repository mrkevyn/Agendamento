package com.gov.ma.saoluis.agendamento.DTO;

import java.util.List;

public record UsuarioLogadoDTO(
        Long id,
        String nome,
        String perfil,
        List<SecretariaDTO> secretarias, // Já estava como lista
        List<SetorDTO> setores,
        Long pontoId,
        Integer numeroPonto,
        String descricaoPonto,
        String token) {}
