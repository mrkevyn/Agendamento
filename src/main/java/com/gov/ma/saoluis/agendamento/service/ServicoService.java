package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.repository.ServicoRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final EnderecoRepository enderecoRepository;

    public ServicoService(ServicoRepository servicoRepository, EnderecoRepository enderecoRepository) {
        this.servicoRepository = servicoRepository;
        this.enderecoRepository = enderecoRepository;
    }

    // Lista todos os serviços
    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    // Buscar serviço por ID
    public Servico buscarPorId(Long id) {
        Optional<Servico> servico = servicoRepository.findById(id);
        return servico.orElse(null); // retorna null se não encontrar
    }

    public List<Servico> listarPorSecretaria(Long secretariaId) {
        return servicoRepository.findBySecretariaId(secretariaId);
    }

    @Transactional
    public Servico vincularEnderecos(Long servicoId, List<Long> enderecoIds) {

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        List<Endereco> enderecos = enderecoRepository.findAllById(
                enderecoIds.stream().distinct().toList()
        );

        if (enderecos.size() != enderecoIds.stream().distinct().count()) {
            throw new RuntimeException("Um ou mais endereços não foram encontrados");
        }

        servico.getEnderecos().addAll(enderecos);

        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico desvincularEnderecos(Long servicoId, List<Long> enderecoIds) {

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        // ids únicos
        List<Long> ids = enderecoIds.stream().distinct().toList();

        // busca e valida se todos existem
        List<Endereco> enderecos = enderecoRepository.findAllById(ids);

        if (enderecos.size() != ids.size()) {
            throw new RuntimeException("Um ou mais endereços não foram encontrados");
        }

        // remove do relacionamento
        servico.getEnderecos().removeAll(enderecos);

        return servicoRepository.save(servico);
    }
}
