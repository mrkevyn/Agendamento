package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.TipoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoAtendimentoRepository extends JpaRepository<TipoAtendimento, Long> {
    Optional<TipoAtendimento> findByNome(String nome);

    List<TipoAtendimento> findByAtivoTrue();
}