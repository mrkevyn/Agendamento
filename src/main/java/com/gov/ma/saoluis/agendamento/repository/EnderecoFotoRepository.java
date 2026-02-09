package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.EnderecoFoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnderecoFotoRepository extends JpaRepository<EnderecoFoto, Long> {

    // 🔎 listar fotos de um endereço
    List<EnderecoFoto> findByEnderecoId(Long enderecoId);

    // ⭐ buscar foto principal (opcional)
    EnderecoFoto findByEnderecoIdAndPrincipalTrue(Long enderecoId);
}
