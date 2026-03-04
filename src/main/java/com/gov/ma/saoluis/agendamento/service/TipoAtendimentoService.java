package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.TipoAtendimentoDTO;
import com.gov.ma.saoluis.agendamento.model.TipoAtendimento;
import com.gov.ma.saoluis.agendamento.repository.TipoAtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoAtendimentoService {

    private final TipoAtendimentoRepository repository;

    public TipoAtendimentoService(TipoAtendimentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TipoAtendimento criar(TipoAtendimentoDTO dto) {
        // Formata para garantir padronização no banco
        String nomeFormatado = dto.nome().trim().toUpperCase();
        String siglaFormatada = dto.sigla().trim().toUpperCase();

        // Valida se já existe para não ter nomes duplicados
        if (repository.findByNome(nomeFormatado).isPresent()) {
            throw new RuntimeException("Já existe um tipo de atendimento com este nome.");
        }

        TipoAtendimento novoTipo = new TipoAtendimento();
        novoTipo.setNome(nomeFormatado);

        // Pega apenas a primeira letra se o admin digitar mais de uma por engano
        novoTipo.setSigla(siglaFormatada.substring(0, 1));

        // Se vier nulo, assume que é true por padrão
        novoTipo.setAtivo(dto.ativo() != null ? dto.ativo() : true);

        return repository.save(novoTipo);
    }

    public List<TipoAtendimento> listarAtivos() {
        return repository.findByAtivoTrue();
    }

    public List<TipoAtendimento> listarTodos() {
        return repository.findAll(); // O Admin precisa ver todos, inclusive os inativos
    }
}