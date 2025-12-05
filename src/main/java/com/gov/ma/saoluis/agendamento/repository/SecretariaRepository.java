package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Secretaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecretariaRepository extends JpaRepository<Secretaria, Integer> {

    // Somente visíveis e ativas
    List<Secretaria> findByVisivelTrueAndAtivoTrue();

    // Buscar por nome (contendo, ignorando maiúsc/minúsc)
    List<Secretaria> findByNomeContainingIgnoreCase(String nome);

    // Buscar por sigla
    Secretaria findBySigla(String sigla);

    // Buscar somente ativas
    List<Secretaria> findByAtivoTrue();

    // Buscar somente visíveis
    List<Secretaria> findByVisivelTrue();
}
