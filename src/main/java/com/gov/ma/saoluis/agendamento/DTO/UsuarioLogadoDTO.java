package com.gov.ma.saoluis.agendamento.DTO;

public record UsuarioLogadoDTO(
        Long id,
        String nome,
        String perfil,
        SecretariaDTO secretaria,
        EnderecoDTO endereco,
        Integer guiche,
        String token) {}
