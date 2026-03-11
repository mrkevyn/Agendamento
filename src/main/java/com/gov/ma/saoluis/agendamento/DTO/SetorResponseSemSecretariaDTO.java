package com.gov.ma.saoluis.agendamento.DTO;

public record SetorResponseSemSecretariaDTO(
        Long id,
        String nome,
        String descricao,
        EnderecoDTO endereco
) {}