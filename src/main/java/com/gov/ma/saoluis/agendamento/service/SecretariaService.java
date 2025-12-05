package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecretariaService {

    private final SecretariaRepository secretariaRepository;

    public SecretariaService(SecretariaRepository secretariaRepository) {
        this.secretariaRepository = secretariaRepository;
    }

    // 1. Buscar TODAS as secretarias
    public List<Secretaria> buscarTodas() {
        return secretariaRepository.findAll();
    }

    // 2. Buscar por ID
    public Optional<Secretaria> buscarPorId(Integer id) {
        return secretariaRepository.findById(id);
    }

    // 3. Buscar somente ativas
    public List<Secretaria> buscarAtivas() {
        return secretariaRepository.findByAtivoTrue();
    }

    // 4. Buscar somente visíveis
    public List<Secretaria> buscarVisiveis() {
        return secretariaRepository.findByVisivelTrue();
    }

    // 5. Buscar ativas e visíveis (mais usado em frontend)
    public List<Secretaria> buscarAtivasEVisiveis() {
        return secretariaRepository.findByVisivelTrueAndAtivoTrue();
    }

    // 6. Buscar por nome
    public List<Secretaria> buscarPorNome(String nome) {
        return secretariaRepository.findByNomeContainingIgnoreCase(nome);
    }

    // 7. Buscar por sigla
    public Secretaria buscarPorSigla(String sigla) {
        return secretariaRepository.findBySigla(sigla);
    }
}
