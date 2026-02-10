package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.config.JwtService;
import com.gov.ma.saoluis.agendamento.config.UsuarioLogadoUtil;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import com.gov.ma.saoluis.agendamento.service.GerenciadorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerenciador")
public class GerenciadorController {

    private final GerenciadorService gerenciadorService;
    private final JwtService jwtService;
    private final GerenciadorRepository gerenciadorRepository;

    public GerenciadorController(GerenciadorService gerenciadorService, JwtService jwtService,
                                 GerenciadorRepository gerenciadorRepository) {
        this.gerenciadorService = gerenciadorService;
        this.jwtService = jwtService;
        this.gerenciadorRepository = gerenciadorRepository;
    }

    // ➤ Criar atendente
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody GerenciadorDTO dto) {
        try {
            Gerenciador novo = gerenciadorService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("mensagem", e.getMessage())
            );
        }
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
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {

        try {
            Gerenciador g = gerenciadorService.login(
                    dto.login(),
                    dto.senha()
            );

            String token = jwtService.gerarToken(
                    g.getId(),
                    g.getPerfil(),
                    g.getSecretaria().getId()
            );

            // 🏢 Monta SecretariaDTO
            SecretariaDTO secretariaDTO = new SecretariaDTO(
                    g.getSecretaria().getId(),
                    g.getSecretaria().getNome(),
                    g.getSecretaria().getSigla()
            );

            return ResponseEntity.ok(
                    new LoginResponseDTO(
                            g.getId(),
                            g.getNome(),
                            g.getPerfil(),
                            token
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario-logado")
    public UsuarioLogadoDTO usuarioLogado(HttpServletRequest request) {

        Long usuarioId = UsuarioLogadoUtil.getUsuarioId();

        if (usuarioId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Gerenciador g = gerenciadorRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 🔹 Token do header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        SecretariaDTO secretariaDTO = new SecretariaDTO(
                g.getSecretaria().getId(),
                g.getSecretaria().getNome(),
                g.getSecretaria().getSigla()
        );

        EnderecoDTO enderecoDTO = null;
        if (g.getEndereco() != null) {
            enderecoDTO = new EnderecoDTO(
                    g.getEndereco().getId(),
                    g.getEndereco().getLogradouro(),
                    g.getEndereco().getNumero(),
                    g.getEndereco().getComplemento(),
                    g.getEndereco().getBairro(),
                    g.getEndereco().getCidade(),
                    g.getEndereco().getUf(),
                    g.getEndereco().getCep()
            );
        }

        return new UsuarioLogadoDTO(
                g.getId(),
                g.getNome(),
                g.getPerfil(),
                secretariaDTO,
                enderecoDTO,   // 👈 agora completo
                g.getGuiche(),
                token
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
