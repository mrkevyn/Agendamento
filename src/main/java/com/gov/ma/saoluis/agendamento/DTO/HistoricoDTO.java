package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDateTime;

public record HistoricoDTO(
        Long id,
        String senha,
        String situacao,
        LocalDateTime horaAgendamento,
        String nomeServico,
        String nomeSetor
) {}