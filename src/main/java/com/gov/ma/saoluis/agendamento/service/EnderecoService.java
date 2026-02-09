package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.EnderecoCreateDTO;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    @Transactional
    public Endereco criar(EnderecoCreateDTO dto) {
        if (dto.logradouro() == null || dto.logradouro().isBlank())
            throw new RuntimeException("Logradouro é obrigatório");
        if (dto.cidade() == null || dto.cidade().isBlank())
            throw new RuntimeException("Cidade é obrigatória");
        if (dto.uf() == null || dto.uf().isBlank())
            throw new RuntimeException("UF é obrigatória");

        Endereco e = new Endereco();
        e.setLogradouro(dto.logradouro());
        e.setNumero(dto.numero());
        e.setBairro(dto.bairro());
        e.setCidade(dto.cidade());
        e.setUf(dto.uf());
        e.setCep(dto.cep());
        e.setComplemento(dto.complemento());
        e.setLatitude(dto.latitude());
        e.setLongitude(dto.longitude());

        return enderecoRepository.save(e);
    }

    @Transactional(readOnly = true)
    public List<Endereco> listarTodos() {
        return enderecoRepository.findAll();
    }
}
