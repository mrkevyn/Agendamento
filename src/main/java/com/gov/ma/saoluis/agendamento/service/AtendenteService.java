package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.AtendenteDTO;
import com.gov.ma.saoluis.agendamento.model.Atendente;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.repository.AtendenteRepository;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendenteService {

    private final AtendenteRepository atendenteRepository;
    private final SecretariaRepository secretariaRepository;

    public AtendenteService(AtendenteRepository atendenteRepository,
                            SecretariaRepository secretariaRepository) {
        this.atendenteRepository = atendenteRepository;
        this.secretariaRepository = secretariaRepository;
    }

    // ➤ Criar atendente
    public Atendente criar(AtendenteDTO dto) {

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        Atendente at = new Atendente();
        at.setNome(dto.nome());
        at.setGuiche(dto.guiche());
        at.setSecretaria(secretaria);

        // usa SIGLA agora
        String acesso = gerarAcesso(dto.nome(), secretaria.getSigla());
        at.setAcesso(acesso);

        return atendenteRepository.save(at);
    }

    // ➤ Atualizar atendente
    public Atendente editar(Long id, AtendenteDTO dto) {

        Atendente at = atendenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        at.setNome(dto.nome());
        at.setGuiche(dto.guiche());
        at.setSecretaria(secretaria);

        // usa SIGLA agora
        at.setAcesso(gerarAcesso(dto.nome(), secretaria.getSigla()));

        return atendenteRepository.save(at);
    }

    // ➤ Listar todos
    public List<Atendente> listarTodos() {
        return atendenteRepository.findAll();
    }

    // ➤ Listar por secretaria
    public List<Atendente> listarPorSecretaria(Long secretariaId) {
        return atendenteRepository.findBySecretariaId(secretariaId);
    }

    // ➤ Buscar por ID
    public Atendente buscarPorId(Long id) {
        return atendenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
    }

    // ➤ Remover
    public void remover(Long id) {
        Atendente at = atendenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
        atendenteRepository.delete(at);
    }

    // Utilitário interno para gerar o campo acesso
    private String gerarAcesso(String nome, String sigla) {
        return nome.replace(" ", "") + "-" + sigla.replace(" ", "");
    }

    public Atendente login(String acesso) {
        return atendenteRepository.findByAcesso(acesso)
                .orElseThrow(() -> new RuntimeException("Acesso inválido"));
    }
}
