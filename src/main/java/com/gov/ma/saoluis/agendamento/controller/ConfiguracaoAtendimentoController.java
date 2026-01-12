package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.service.ConfiguracaoAtendimentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/configuracoes-atendimento")
public class ConfiguracaoAtendimentoController {

    private final ConfiguracaoAtendimentoService service;

    public ConfiguracaoAtendimentoController(ConfiguracaoAtendimentoService service) {
        this.service = service;
    }

    // 🔹 Criar configuração
    @PostMapping
    public ResponseEntity<ConfiguracaoAtendimento> criar(
            @RequestBody ConfiguracaoAtendimento configuracao
    ) {
        ConfiguracaoAtendimento salva = service.salvar(configuracao);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    // 🔹 Atualizar configuração
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracaoAtendimento> atualizar(
            @PathVariable Long id,
            @RequestBody ConfiguracaoAtendimento configuracao
    ) {
        ConfiguracaoAtendimento atualizada = service.atualizar(id, configuracao);
        return ResponseEntity.ok(atualizada);
    }

    // 🔹 Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracaoAtendimento> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // 🔹 Listar configurações ativas por secretaria
    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<ConfiguracaoAtendimento>> listarPorSecretaria(
            @PathVariable Long secretariaId
    ) {
        return ResponseEntity.ok(service.listarPorSecretaria(secretariaId));
    }

    // 🔹 Desativar configuração
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id
    ) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 Validar disponibilidade (usado pelo agendamento)
    @GetMapping("/validar-disponibilidade")
    public ResponseEntity<ConfiguracaoAtendimento> validarDisponibilidade(
            @RequestParam Long secretariaId,
            @RequestParam LocalDate data,
            @RequestParam LocalTime hora
    ) {
        return ResponseEntity.ok(
                service.validarDisponibilidade(secretariaId, data, hora)
        );
    }
}
