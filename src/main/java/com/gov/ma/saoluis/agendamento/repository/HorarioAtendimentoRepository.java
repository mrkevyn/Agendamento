package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.HorarioAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtendimentoRepository
        extends JpaRepository<HorarioAtendimento, Long> {
}
