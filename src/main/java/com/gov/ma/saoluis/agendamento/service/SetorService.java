package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.SetorCreateDTO;
import com.gov.ma.saoluis.agendamento.DTO.SetorResponseDTO;
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
        setor.setSecretaria(secretaria); // ⬅️ Realiza o vínculo com a secretaria

        setor = setorRepository.save(setor);

        return new SetorResponseDTO(setor.getId(), setor.getNome(), setor.getDescricao());
    }

    public List<SetorResponseDTO> listarPorSetor(Long setorId) {
        return setorRepository.findByEnderecoId(setorId)
                .stream()
                .map(s -> new SetorResponseDTO(s.getId(), s.getNome(), s.getDescricao()))
                .toList();
    }

    @Transactional
    public void deletar(Long id) {
        if (!setorRepository.existsById(id)) {
            throw new RuntimeException("Setor não encontrado");
        }
        setorRepository.deleteById(id);
    }
}