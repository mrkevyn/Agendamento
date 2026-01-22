package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoAppRequest(
        Long usuarioId,
        Long servicoId,
        Long configuracaoId,
        LocalDate data,
        LocalTime hora,
        String tipoAtendimento
) {}
