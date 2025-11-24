package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.Usuario;
import com.gov.ma.saoluis.agendamento.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        System.out.println("Executando select * from usuario"); // teste
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }
}
