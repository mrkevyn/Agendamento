package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.LoginRequestDTO;
import com.gov.ma.saoluis.agendamento.DTO.LoginResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.SecretariaDTO;
import com.gov.ma.saoluis.agendamento.DTO.SetorDTO;
import com.gov.ma.saoluis.agendamento.config.JwtService;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LogService logService; // Traga o LogService para cá ou mantenha no seu Service

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        try {
            // 1. Cria o token não autenticado com as credenciais recebidas
            var authToken = new UsernamePasswordAuthenticationToken(dto.login(), dto.senha());

            // 2. O AuthenticationManager assume o controle (chama o UserDetailsService e compara a senha)
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 3. (OPCIONAL NO LOGIN STATELESS, MAS BOA PRÁTICA) Seta a autenticação no contexto atual
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. Recupera a entidade gerenciador autenticada
            Gerenciador g = (Gerenciador) authentication.getPrincipal();

            // 5. Extração para JWT e DTOs
            List<Long> secretariaIds = g.getSecretarias().stream()
                    .map(Secretaria::getId)
                    .collect(Collectors.toList());

            String token = jwtService.gerarToken(g.getId(), g.getPerfil(), secretariaIds);

            List<SecretariaDTO> secretariasDTO = g.getSecretarias().stream()
                    .map(sec -> new SecretariaDTO(sec.getId(), sec.getNome(), sec.getSigla()))
                    .toList();

            List<SetorDTO> setoresDTO = g.getSetores().stream()
                    .map(set -> new SetorDTO(
                            set.getId(),
                            set.getNome(),
                            set.getSecretaria() != null ? set.getSecretaria().getId() : null
                    )).toList();

            // 🔹 REGISTRAR LOG DE LOGIN
            String nomesSecretarias = g.getSecretarias().stream()
                    .map(Secretaria::getNome)
                    .collect(Collectors.joining(", "));

            // Aqui pegamos o nome dos setores usando a entidade Setor (e não o DTO)
            String nomesSetores = g.getSetores().stream()
                    .map(setor -> setor.getNome())
                    .collect(Collectors.joining(", "));

            // 🔹 REGISTRAR LOG DE LOGIN
            // (Você pode criar um método no LogService que receba o objeto 'g' pronto)
            logService.registrar(
                    g.getId(),
                    g.getPerfil(),
                    "LOGIN",
                    "Nome: " + g.getNome() +
                            "; Email: " + g.getEmail() +
                            "; Secretarias: [" + nomesSecretarias + "]" +
                            "; Setores: [" + nomesSetores + "]"
            );

            return ResponseEntity.ok(
                    new LoginResponseDTO(
                            g.getId(), g.getNome(), g.getPerfil(), token, secretariasDTO, setoresDTO
                    )
            );

        } catch (org.springframework.security.core.AuthenticationException e) {
            // Se o Spring detectar senha errada ou usuário não encontrado, cai aqui
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login ou senha inválidos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro interno: " + e.getMessage());
        }
    }
}