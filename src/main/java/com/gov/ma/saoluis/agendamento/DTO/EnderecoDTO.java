package com.gov.ma.saoluis.agendamento.DTO;

import java.math.BigDecimal;

public record EnderecoDTO(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        BigDecimal latitude,
        BigDecimal longitude
) {}
