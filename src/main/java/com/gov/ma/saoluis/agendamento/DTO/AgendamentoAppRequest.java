package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoAppRequest(
        Long usuarioId,
        Long servicoId,
        Long setorId,
        LocalDate data,
        LocalTime hora,
        Long tipoAtendimentoId
) {}
