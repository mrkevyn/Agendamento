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

    // Rota para o cidadão/aplicativo ver apenas as opções ativas no <select>
    @GetMapping("/ativos")
    public ResponseEntity<List<TipoAtendimento>> listarAtivos() {
        return ResponseEntity.ok(service.listarAtivos());
    }

    // Rota para o painel do ADMIN listar todos
    @GetMapping
    public ResponseEntity<List<TipoAtendimento>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }
}