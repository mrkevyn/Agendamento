package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final GerenciadorRepository repository;

    public AutenticacaoService(GerenciadorRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca por CPF ou Email, exatamente como você fazia antes
        return repository.findByCpfOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}