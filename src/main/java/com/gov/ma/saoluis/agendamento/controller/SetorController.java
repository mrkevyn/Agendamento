package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.SetorCreateDTO;
import com.gov.ma.saoluis.agendamento.DTO.SetorResponseDTO;
import com.gov.ma.saoluis.agendamento.service.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
public class SetorController {

    @Autowired
    private SetorService setorService;

    @PostMapping
    public ResponseEntity<SetorResponseDTO> criar(@RequestBody SetorCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setorService.salvar(dto));
    }

    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<SetorResponseDTO>> listarPorEndereco(@PathVariable Long setorId) {
        return ResponseEntity.ok(setorService.listarPorSetor(setorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        setorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-secretaria/{secretariaId}")
    public ResponseEntity<List<SetorResponseDTO>> listarPorSecretaria(
            @PathVariable Long secretariaId) {

        return ResponseEntity.ok(setorService.listarPorSecretaria(secretariaId));
    }
}