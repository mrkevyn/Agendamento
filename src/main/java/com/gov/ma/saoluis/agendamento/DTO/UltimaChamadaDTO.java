package com.gov.ma.saoluis.agendamento.DTO;

import java.sql.Timestamp;

public record UltimaChamadaDTO(
        Long agendamentoId,         // ✅ ID DO AGENDAMENTO
        String senha,
        String tipoAtendimento,
        Timestamp horaChamada,
        Long usuarioId,
        String usuarioNome,
        Long servicoId,
        String servicoNome,
        Integer guiche
) {}
