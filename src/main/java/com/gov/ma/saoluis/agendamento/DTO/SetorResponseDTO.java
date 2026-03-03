package com.gov.ma.saoluis.agendamento.DTO;

public record SetorResponseDTO(
        Long id,
        String nome,
        String descricao,
        EnderecoDTO endereco
) {}