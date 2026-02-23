package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.model.Guiche;
import com.gov.ma.saoluis.agendamento.repository.GuicheRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guiches")// Permite que o Vue acesse o endpoint
public class GuicheController {

    private final GuicheRepository guicheRepository;

    public GuicheController(GuicheRepository guicheRepository) {
        this.guicheRepository = guicheRepository;
    }

    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<Guiche>> listarPorSetor(@PathVariable Long setorId) {
        List<Guiche> guiches = guicheRepository.findBySetorId(setorId);
        return ResponseEntity.ok(guiches);
    }
}