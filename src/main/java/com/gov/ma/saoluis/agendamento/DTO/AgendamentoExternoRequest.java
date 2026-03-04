package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoExternoRequest(
        Long servicoId,
        Long setorId,
        LocalDate data,
        LocalTime hora,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String celular,
        String email,
        Long tipoAtendimentoId
) {}