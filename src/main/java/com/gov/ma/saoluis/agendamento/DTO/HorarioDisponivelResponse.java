package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalTime;

public record HorarioDisponivelResponse(
        LocalTime hora,
        int vagasDisponiveis
) {}
