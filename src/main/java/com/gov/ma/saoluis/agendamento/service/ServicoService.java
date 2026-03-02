package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.ServicoResponseDTO;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.model.Setor;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.repository.ServicoRepository;
import com.gov.ma.saoluis.agendamento.repository.SetorRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final SetorRepository setorRepository;

    public ServicoService(ServicoRepository servicoRepository, SetorRepository setorRepository) {
        this.servicoRepository = servicoRepository;
        this.setorRepository = setorRepository;
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

    public List<ServicoResponseDTO> listarPorSetor(Long setorId) {

        return servicoRepository.findBySetoresId(setorId)
                .stream()
                .map(s -> new ServicoResponseDTO(
                        s.getId(),
                        s.getNome(),
                        s.getDescricao()
                ))
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
