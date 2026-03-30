package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.TipoAtendimentoDTO;
import com.gov.ma.saoluis.agendamento.model.TipoAtendimento;
import com.gov.ma.saoluis.agendamento.service.TipoAtendimentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-atendimento")
public class TipoAtendimentoController {

    private final TipoAtendimentoService service;

    public TipoAtendimentoController(TipoAtendimentoService service) {
        this.service = service;
    }

    // Rota para o ADMIN cadastrar um novo tipo
    @PostMapping
    public ResponseEntity<TipoAtendimento> cadastrar(@RequestBody TipoAtendimentoDTO dto) {
        TipoAtendimento salvo = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoAtendimento> atualizar(
            @PathVariable Long id,
            @RequestBody TipoAtendimentoDTO dto) {

        TipoAtendimento atualizado = service.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/secretaria/{secretariaId}/ativos")
    public ResponseEntity<List<TipoAtendimentoDTO>> listarAtivosPorSecretaria(@PathVariable Long secretariaId) {
        List<TipoAtendimentoDTO> dtos = service.listarAtivosPorSecretaria(secretariaId)
                .stream()
                .map(t -> new TipoAtendimentoDTO(
                        t.getId(),             // 1. id (do tipo)
                        t.getAtivo(),
                        t.getNome(),// 3. ativo
                        secretariaId,          // 4. secretariaId (que veio no Path)
                        t.getPeso()            // 5. peso
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // Rota para o painel do ADMIN listar todos de uma secretaria específica
    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<TipoAtendimento>> listarTodosPorSecretaria(@PathVariable Long secretariaId) {
        return ResponseEntity.ok(service.listarTodosPorSecretaria(secretariaId));
    }
}