package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.model.Setor;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import com.gov.ma.saoluis.agendamento.repository.SetorRepository;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private SecretariaRepository secretariaRepository;

    @Transactional
    public SetorResponseDTO salvar(SetorCreateDTO dto) {
        Endereco endereco = enderecoRepository.findById(dto.enderecoId())
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        Setor setor = new Setor();
        setor.setNome(dto.nome());
        setor.setDescricao(dto.descricao());
        setor.setEndereco(endereco);
        setor.setSecretaria(secretaria);

        setor = setorRepository.save(setor);

        return new SetorResponseDTO(
                setor.getId(),
                setor.getNome(),
                setor.getDescricao(),
                new EnderecoDTO(
                        setor.getEndereco().getLogradouro(),
                        setor.getEndereco().getNumero(),
                        setor.getEndereco().getComplemento(),
                        setor.getEndereco().getBairro(),
                        setor.getEndereco().getCidade(),
                        setor.getEndereco().getUf(),
                        setor.getEndereco().getCep()
                ),
                new SecretariaDTO(
                        secretaria.getId(),
                        secretaria.getNome(),
                        secretaria.getSigla()
                )
        );
    }

    public List<SetorResponseDTO> listarPorSetor(Long setorId) {
        return setorRepository.findByEnderecoId(setorId)
                .stream()
                .map(setor -> new SetorResponseDTO(
                        setor.getId(),
                        setor.getNome(),
                        setor.getDescricao(),
                        new EnderecoDTO(
                                setor.getEndereco().getLogradouro(),
                                setor.getEndereco().getNumero(),
                                setor.getEndereco().getComplemento(),
                                setor.getEndereco().getBairro(),
                                setor.getEndereco().getCidade(),
                                setor.getEndereco().getUf(),
                                setor.getEndereco().getCep()
                        ),
                        new SecretariaDTO(
                                setor.getSecretaria().getId(),
                                setor.getSecretaria().getNome(),
                                setor.getSecretaria().getSigla()
                        )
                ))
                .toList();
    }

    @Transactional
    public void deletar(Long id) {
        if (!setorRepository.existsById(id)) {
            throw new RuntimeException("Setor não encontrado");
        }
        setorRepository.deleteById(id);
    }

    public List<SetorResponseDTO> listarPorSecretaria(Long secretariaId) {
        return setorRepository
                .findBySecretariaId(secretariaId)
                .stream()
                .map(setor -> new SetorResponseDTO(
                        setor.getId(),
                        setor.getNome(),
                        setor.getDescricao(),
                        new EnderecoDTO(
                                setor.getEndereco().getLogradouro(),
                                setor.getEndereco().getNumero(),
                                setor.getEndereco().getComplemento(),
                                setor.getEndereco().getBairro(),
                                setor.getEndereco().getCidade(),
                                setor.getEndereco().getUf(),
                                setor.getEndereco().getCep()
                        ),
                        new SecretariaDTO(
                                setor.getSecretaria().getId(),
                                setor.getSecretaria().getNome(),
                                setor.getSecretaria().getSigla()
                        )
                ))
                .toList();
    }
}