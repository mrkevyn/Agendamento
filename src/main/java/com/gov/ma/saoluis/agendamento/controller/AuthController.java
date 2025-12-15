package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.LoginRequestDTO;
import com.gov.ma.saoluis.agendamento.DTO.LoginResponseDTO;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.service.GerenciadorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GerenciadorService gerenciadorService;

    public AuthController(GerenciadorService gerenciadorService) {
        this.gerenciadorService = gerenciadorService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {

        Gerenciador gerenciador = gerenciadorService.login(
                dto.login(),   // cpf ou email
                dto.senha()
        );

        return new LoginResponseDTO(
                gerenciador.getId(),
                gerenciador.getNome(),
                gerenciador.getPerfil(),                 // 👈 PERFIL
                gerenciador.getSecretaria().getId(),
                gerenciador.getGuiche()
        );
    }

}
