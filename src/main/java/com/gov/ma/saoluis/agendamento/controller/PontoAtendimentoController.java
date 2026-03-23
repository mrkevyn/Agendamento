package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.model.PontoAtendimento;
import com.gov.ma.saoluis.agendamento.repository.PontoAtendimentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pontos-atendimento")
public class PontoAtendimentoController {

    private final PontoAtendimentoRepository pontoAtendimentoRepository;

    public PontoAtendimentoController(PontoAtendimentoRepository pontoAtendimentoRepository) {
        this.pontoAtendimentoRepository = pontoAtendimentoRepository;
    }

    /**
     * Lista os pontos de atendimento (Guichês, Consultórios, etc) vinculados a um setor.
     * Utilizado no frontend para o atendente selecionar onde está trabalhando.
     */
    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<PontoAtendimento>> listarPorSetor(@PathVariable Long setorId) {
        List<PontoAtendimento> pontos = pontoAtendimentoRepository.findBySetorId(setorId);
        return ResponseEntity.ok(pontos);
    }

    /**
     * Retorna todos os pontos cadastrados (Útil para telas de administração)
     */
    @GetMapping
    public ResponseEntity<List<PontoAtendimento>> listarTodos() {
        return ResponseEntity.ok(pontoAtendimentoRepository.findAll());
    }
}