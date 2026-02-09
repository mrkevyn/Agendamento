package com.gov.ma.saoluis.agendamento.DTO;

import java.math.BigDecimal;

public record EnderecoCreateDTO(
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String uf,
        String cep,
        String complemento,
        BigDecimal latitude,
        BigDecimal longitude
) {}
