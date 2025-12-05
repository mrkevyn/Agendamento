package com.gov.ma.saoluis.agendamento.DTO;

import com.gov.ma.saoluis.agendamento.model.Agendamento;

public record ChamadaResponseDTO(
        Agendamento agendamento,
        String mensagem
) {}
