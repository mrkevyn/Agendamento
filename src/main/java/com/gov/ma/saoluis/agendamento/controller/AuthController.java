package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.LoginRequestDTO;
import com.gov.ma.saoluis.agendamento.DTO.LoginResponseDTO;
import com.gov.ma.saoluis.agendamento.model.Atendente;
import com.gov.ma.saoluis.agendamento.service.AtendenteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AtendenteService atendenteService;

    public AuthController(AtendenteService atendenteService) {
        this.atendenteService = atendenteService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {

        Atendente at = atendenteService.login(dto.acesso());

        return new LoginResponseDTO(
                at.getId(),
                at.getNome(),
                at.getAcesso(),
                at.getSecretaria().getId(),
                at.getGuiche()
        );
    }
}
