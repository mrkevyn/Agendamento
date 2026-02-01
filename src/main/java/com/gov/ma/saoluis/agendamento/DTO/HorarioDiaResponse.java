package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalTime;

public record HorarioDiaResponse(
        LocalTime hora,
        int capacidade,
        int reservados,
        int vagas,
        boolean lotado
) {}