package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.config.JwtService;
import com.gov.ma.saoluis.agendamento.config.UsuarioLogadoUtil;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.model.Setor;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import com.gov.ma.saoluis.agendamento.service.GerenciadorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gerenciador")
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

    // Criar atendente
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

    // Editar atendente (N Secretarias : N Setores)
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody GerenciadorDTO dto) {

        // 1. Salva no banco (Lógica de Service N:N)
        Gerenciador atualizado = gerenciadorService.editar(id, dto);

        // 2. Monta a resposta limpa para o JSON
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("id", atualizado.getId());
        resposta.put("nome", atualizado.getNome());
        resposta.put("cpf", atualizado.getCpf());
        resposta.put("email", atualizado.getEmail());

        // Ajuste no Guichê: Extraindo apenas o número para o JSON
        // Se o seu Vue precisar do ID para formulários, você pode adicionar resposta.put("guicheId", ...)
        resposta.put("guiche", atualizado.getPontoAtendimento() != null ? atualizado.getPontoAtendimento().getNumero() : null);

        resposta.put("perfil", atualizado.getPerfil());

        // Agora extrai a lista de nomes das SECRETARIAS (plural)
        List<String> nomesSecretarias = atualizado.getSecretarias().stream()
                .map(Secretaria::getNome)
                .toList();
        resposta.put("secretarias", nomesSecretarias);

        // Extrai a lista de nomes dos SETORES (plural)
        List<String> nomesSetores = atualizado.getSetores().stream()
                .map(Setor::getNome)
                .toList();
        resposta.put("setores", nomesSetores);

        return ResponseEntity.ok(resposta);
    }

    // Listar todos
    @GetMapping
    public ResponseEntity<List<Gerenciador>> listarTodos() {
        return ResponseEntity.ok(gerenciadorService.listarTodos());
    }

    // Listar por secretaria
    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<Gerenciador>> listarPorSecretaria(@PathVariable Long secretariaId) {
        return ResponseEntity.ok(gerenciadorService.listarPorSecretaria(secretariaId));
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Gerenciador> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gerenciadorService.buscarPorId(id));
    }

    // Remover
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        gerenciadorService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario-logado")
    public UsuarioLogadoDTO usuarioLogado(HttpServletRequest request) {

        Long usuarioId = UsuarioLogadoUtil.getUsuarioId();

        if (usuarioId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Gerenciador g = gerenciadorRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Token do header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Transforma as Secretarias (N) em DTOs
        List<SecretariaDTO> secretariasDTO = g.getSecretarias().stream()
                .map(sec -> new SecretariaDTO(sec.getId(), sec.getNome(), sec.getSigla()))
                .collect(Collectors.toList());

        // Transforma os Setores (N) em DTOs (plural agora!)
        List<SetorDTO> setoresDTO = g.getSetores().stream()
                .map(set -> new SetorDTO(
                        set.getId(),
                        set.getNome(),
                        set.getSecretaria().getId()
                ))
                .collect(Collectors.toList());

        Integer numeroPonto = null;
        String descricaoPonto = null;
        Long pontoId = null;

        if (g.getPontoAtendimento() != null) {
            pontoId = g.getPontoAtendimento().getId();
            numeroPonto = g.getPontoAtendimento().getNumero();
            descricaoPonto = g.getPontoAtendimento().getDescricao();
        }

        return new UsuarioLogadoDTO(
                g.getId(),
                g.getNome(),
                g.getPerfil(),
                secretariasDTO,
                setoresDTO, // Agora enviando a lista completa de setores
                pontoId,        // Adicionado ID do ponto
                numeroPonto,    // Número (ex: 1)
                descricaoPonto,
                token
        );
    }

    @PatchMapping("/{id}/guiche")
    public ResponseEntity<?> atualizarGuiche(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        Long guicheId = payload.get("guicheId");
        gerenciadorService.atualizarGuiche(id, guicheId); // Apenas executa

        // Retorna um JSON simples de sucesso em vez da entidade Gerenciador
        return ResponseEntity.ok(Map.of("mensagem", "Guichê atualizado com sucesso!"));
    }

    @PostMapping("/{id}/logout-guiche")
    public ResponseEntity<?> logoutGuiche(@PathVariable Long id) {
        gerenciadorService.deslogarGuiche(id);
        return ResponseEntity.ok().build();
    }
}
