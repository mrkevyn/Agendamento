package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.TipoAtendimentoDTO;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.model.TipoAtendimento;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import com.gov.ma.saoluis.agendamento.repository.TipoAtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoAtendimentoService {

    private final TipoAtendimentoRepository repository;
    private final SecretariaRepository secretariaRepository; // Injetado para buscar a Secretaria

    public TipoAtendimentoService(TipoAtendimentoRepository repository, SecretariaRepository secretariaRepository) {
        this.repository = repository;
        this.secretariaRepository = secretariaRepository;
    }

    @Transactional(readOnly = true)
    public TipoAtendimento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de atendimento não encontrado"));
    }

    @Transactional
    public TipoAtendimento criar(TipoAtendimentoDTO dto) {
        // 1. Busca a secretaria
        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        // 2. Formata o nome
        String nomeFormatado = dto.nome().trim().toUpperCase();

        // 3. Valida se já existe esse nome NESSA secretaria
        if (repository.findByNomeAndSecretaria_Id(nomeFormatado, secretaria.getId()).isPresent()) {
            throw new RuntimeException("Já existe um tipo de atendimento com este nome nesta secretaria.");
        }

        // 4. Monta a entidade
        TipoAtendimento novoTipo = new TipoAtendimento();
        novoTipo.setNome(nomeFormatado);
        novoTipo.setSecretaria(secretaria);

        // 5. Lógica inteligente da Sigla e do Peso
        if (nomeFormatado.startsWith("NORMAL")) {
            novoTipo.setSigla("N");
            novoTipo.setPeso(0); // REGRA DE NEGÓCIO: Normal é sempre peso 0, ignora o que vier no DTO
        } else {
            // REGRA DE NEGÓCIO: Se não é NORMAL, o administrador é OBRIGADO a informar o peso (> 0)
            if (dto.peso() == null || dto.peso() <= 0) {
                throw new RuntimeException("O peso da prioridade é obrigatório e deve ser maior que zero.");
            }

            // Define a sigla
            if (nomeFormatado.startsWith("PRIORIDADE")) {
                novoTipo.setSigla("P");
            } else {
                novoTipo.setSigla(nomeFormatado.substring(0, 1)); // Fallback da 1ª letra
            }

            // Atribui o peso validado
            novoTipo.setPeso(dto.peso());
        }

        novoTipo.setAtivo(dto.ativo() != null ? dto.ativo() : true);

        return repository.save(novoTipo);
    }

    @Transactional
    public TipoAtendimento atualizar(Long id, TipoAtendimentoDTO dto) {
        // 1. Busca o tipo existente no banco
        TipoAtendimento existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de atendimento não encontrado"));

        // 2. Formata o nome
        String nomeFormatado = dto.nome().trim().toUpperCase();

        // 3. Valida se já existe esse nome NESSA secretaria (ignorando o próprio registro que está sendo editado)
        repository.findByNomeAndSecretaria_Id(nomeFormatado, existente.getSecretaria().getId())
                .ifPresent(encontrado -> {
                    if (!encontrado.getId().equals(existente.getId())) {
                        throw new RuntimeException("Já existe outro tipo de atendimento com este nome nesta secretaria.");
                    }
                });

        // 4. Atualiza o nome
        existente.setNome(nomeFormatado);

        // 5. Reaplica a lógica inteligente da Sigla e do Peso
        if (nomeFormatado.startsWith("NORMAL")) {
            existente.setSigla("N");
            existente.setPeso(0); // Força zero
        } else {
            if (dto.peso() == null || dto.peso() <= 0) {
                throw new RuntimeException("O peso da prioridade é obrigatório e deve ser maior que zero.");
            }

            if (nomeFormatado.startsWith("PRIORIDADE")) {
                existente.setSigla("P");
            } else {
                existente.setSigla(nomeFormatado.substring(0, 1));
            }

            existente.setPeso(dto.peso());
        }

        // 6. Atualiza o status (Ativo/Inativo)
        if (dto.ativo() != null) {
            existente.setAtivo(dto.ativo());
        }

        // Nota: Não atualizamos a secretariaId aqui. O tipo de atendimento pertence à secretaria onde foi criado.

        return repository.save(existente);
    }

    public List<TipoAtendimento> listarAtivosPorSecretaria(Long secretariaId) {
        return repository.findBySecretaria_IdAndAtivoTrue(secretariaId);
    }

    public List<TipoAtendimento> listarTodosPorSecretaria(Long secretariaId) {
        return repository.findBySecretaria_Id(secretariaId);
    }
}