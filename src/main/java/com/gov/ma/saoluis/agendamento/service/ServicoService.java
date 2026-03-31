package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.ServicoResponseDTO;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.Setor;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import com.gov.ma.saoluis.agendamento.repository.ServicoSaudeRepository;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.repository.ServicoRepository;
import com.gov.ma.saoluis.agendamento.repository.SetorRepository;

import java.util.*;
import java.util.stream.Stream;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final SetorRepository setorRepository;
    private ServicoSaudeRepository servicoSaudeRepository;
    private GerenciadorRepository gerenciadorRepository;

    public ServicoService(ServicoRepository servicoRepository, SetorRepository setorRepository, ServicoSaudeRepository servicoSaudeRepository, GerenciadorRepository gerenciadorRepository) {
        this.servicoRepository = servicoRepository;
        this.setorRepository = setorRepository;
        this.servicoSaudeRepository = servicoSaudeRepository;
        this.gerenciadorRepository = gerenciadorRepository;
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

    private boolean podeAtenderServico(Gerenciador gerenciador, Servico servico) {
        if (servico == null || gerenciador == null) return false;

        Set<Gerenciador> gerenciadoresDoServico = servico.getGerenciadores();

        // 1. Se o serviço não tem NENHUM gerenciador vinculado no banco,
        // ele é público para todos os atendentes do setor.
        if (gerenciadoresDoServico == null || gerenciadoresDoServico.isEmpty()) {
            return true;
        }

        // 2. Se o serviço TEM gerenciadores vinculados, verificamos se o
        // gerenciador logado (ID) está entre eles.
        return gerenciadoresDoServico.stream()
                .anyMatch(g -> g.getId().equals(gerenciador.getId()));
    }

    public List<ServicoResponseDTO> listarPorSetor(Long setorId, Long gerenciadorId) {
        Gerenciador gerenciador = gerenciadorRepository.findById(gerenciadorId)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        // Pegamos a lista de serviços que o atendente TEM vinculados a ele
        Set<Servico> servicosDoGerenciador = gerenciador.getServicos();

        // A LÓGICA MUDOU AQUI:
        // Se o atendente JÁ TEM uma relação de serviços exclusivos...
        if (servicosDoGerenciador != null && !servicosDoGerenciador.isEmpty()) {
            return servicosDoGerenciador.stream()
                    // Garantimos que o serviço exclusivo pertença ao setor solicitado
                    .filter(s -> s.getSetores().stream().anyMatch(setor -> setor.getId().equals(setorId)))
                    .map(s -> new ServicoResponseDTO(s.getId(), s.getNome(), s.getDescricao()))
                    .toList();
        }

        // Caso contrário (ele não tem relação exclusiva), ele vê os serviços gerais do setor
        return servicoRepository.findBySetoresId(setorId)
                .stream()
                .filter(s -> s.getGerenciadores() == null || s.getGerenciadores().isEmpty())
                .map(s -> new ServicoResponseDTO(s.getId(), s.getNome(), s.getDescricao()))
                .toList();
    }

    @Transactional
    public Servico vincularSetores(Long servicoId, List<Long> setorIds) {

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        List<Setor> setores = setorRepository.findAllById(
                setorIds.stream().distinct().toList()
        );

        if (setores.size() != setorIds.stream().distinct().count()) {
            throw new RuntimeException("Um ou mais endereços não foram encontrados");
        }

        servico.getSetores().addAll(setores);

        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico desvincularSetores(Long servicoId, List<Long> setorIds) {

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        // ids únicos
        List<Long> ids = setorIds.stream().distinct().toList();

        // busca e valida se todos existem
        List<Setor> setores = setorRepository.findAllById(ids);

        if (setores.size() != ids.size()) {
            throw new RuntimeException("Um ou mais endereços não foram encontrados");
        }

        // remove do relacionamento
        servico.getSetores().removeAll(setores);

        return servicoRepository.save(servico);
    }
}
