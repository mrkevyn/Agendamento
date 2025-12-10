package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.AtendenteDTO;
import com.gov.ma.saoluis.agendamento.model.Atendente;
import com.gov.ma.saoluis.agendamento.service.AtendenteService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atendentes")
public class AtendenteController {

    private final AtendenteService atendenteService;

    public AtendenteController(AtendenteService atendenteService) {
        this.atendenteService = atendenteService;
    }

    // ➤ Criar atendente
    @PostMapping
    public ResponseEntity<Atendente> criar(@RequestBody AtendenteDTO dto) {
        Atendente novo = atendenteService.criar(dto);
        return ResponseEntity.ok(novo);
    }

    // ➤ Editar atendente
    @PutMapping("/{id}")
    public ResponseEntity<Atendente> editar(@PathVariable Long id, @RequestBody AtendenteDTO dto) {
        Atendente atualizado = atendenteService.editar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    // ➤ Listar todos
    @GetMapping
    public ResponseEntity<List<Atendente>> listarTodos() {
        return ResponseEntity.ok(atendenteService.listarTodos());
    }

    // ➤ Listar por secretaria
    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<Atendente>> listarPorSecretaria(@PathVariable Long secretariaId) {
        return ResponseEntity.ok(atendenteService.listarPorSecretaria(secretariaId));
    }

    // ➤ Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Atendente> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atendenteService.buscarPorId(id));
    }

    // ➤ Remover
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        atendenteService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ➤ Login pelo campo acesso
    @PostMapping("/login")
    public ResponseEntity<Atendente> login(@RequestBody LoginRequest request) {
        Atendente atendente = atendenteService.login(request.getAcesso());
        return ResponseEntity.ok(atendente);
    }

    // DTO interno para receber JSON
    @Setter
    @Getter
    public static class LoginRequest {
        private String acesso;

    }
}
