package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.AtualizarGuicheDTO;
import com.gov.ma.saoluis.agendamento.DTO.GerenciadorDTO;
import com.gov.ma.saoluis.agendamento.DTO.LoginRequestDTO;
import com.gov.ma.saoluis.agendamento.DTO.LoginResponseDTO;
import com.gov.ma.saoluis.agendamento.config.JwtService;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.service.GerenciadorService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador")
public class GerenciadorController {

    private final GerenciadorService gerenciadorService;
    private final JwtService jwtService;

    public GerenciadorController(GerenciadorService gerenciadorService, JwtService jwtService) {
        this.gerenciadorService = gerenciadorService;
        this.jwtService = jwtService;
    }

    // ➤ Criar atendente
    @PostMapping
    public ResponseEntity<Gerenciador> criar(@RequestBody GerenciadorDTO dto) {
        Gerenciador novo = gerenciadorService.criar(dto);
        return ResponseEntity.ok(novo);
    }

    // ➤ Editar atendente
    @PutMapping("/{id}")
    public ResponseEntity<Gerenciador> editar(@PathVariable Long id, @RequestBody GerenciadorDTO dto) {
        Gerenciador atualizado = gerenciadorService.editar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    // ➤ Listar todos
    @GetMapping
    public ResponseEntity<List<Gerenciador>> listarTodos() {
        return ResponseEntity.ok(gerenciadorService.listarTodos());
    }

    // ➤ Listar por secretaria
    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<Gerenciador>> listarPorSecretaria(@PathVariable Long secretariaId) {
        return ResponseEntity.ok(gerenciadorService.listarPorSecretaria(secretariaId));
    }

    // ➤ Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Gerenciador> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gerenciadorService.buscarPorId(id));
    }

    // ➤ Remover
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        gerenciadorService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ➤ Login por CPF ou Email + Senha
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto
    ) {

        Gerenciador g = gerenciadorService.login(
                dto.login(),
                dto.senha()
        );

        // 🔐 Gera token JWT
        String token = jwtService.gerarToken(
                g.getId(),
                g.getPerfil()
        );

        return ResponseEntity.ok(
                new LoginResponseDTO(
                        g.getId(),
                        g.getNome(),
                        g.getPerfil(),
                        g.getSecretaria().getId(),
                        g.getGuiche(),
                        token
                )
        );
    }

    @PutMapping("/{id}/guiche")
    public ResponseEntity<Gerenciador> atualizarGuiche(
            @PathVariable Long id,
            @RequestBody AtualizarGuicheDTO dto
    ) {
        Gerenciador atualizado = gerenciadorService.atualizarGuiche(id, dto.guiche());
        return ResponseEntity.ok(atualizado);
    }
}
