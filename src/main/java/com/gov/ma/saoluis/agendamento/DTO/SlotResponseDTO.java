package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalTime;

public record SlotResponseDTO(
        LocalTime hora,
        int vagasDisponiveis
) {}