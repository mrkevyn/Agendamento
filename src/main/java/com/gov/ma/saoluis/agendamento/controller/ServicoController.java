package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.VincularEnderecosDTO;
import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.service.ServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/agendamento/api/servico")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    // Listar todos os serviços
    @GetMapping("/listar-todos")
    public ResponseEntity<List<Servico>> listarTodos() {
        try {
            List<Servico> servicos = servicoService.listarTodos();
            return ResponseEntity.ok(servicos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // Buscar serviço por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Long id) {
        try {
            Servico servico = servicoService.buscarPorId(id);
            if (servico != null) {
                return ResponseEntity.ok(servico);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/servicos/{id}/enderecos")
    public ResponseEntity<Servico> vincularEnderecos(
            @PathVariable Long id,
            @RequestBody VincularEnderecosDTO dto
    ) {
        return ResponseEntity.ok(servicoService.vincularEnderecos(id, dto.enderecoIds()));
    }

    @PutMapping("/servicos/{id}/enderecos/remover")
    public ResponseEntity<Servico> desvincularEnderecos(
            @PathVariable Long id,
            @RequestBody VincularEnderecosDTO dto
    ) {
        return ResponseEntity.ok(servicoService.desvincularEnderecos(id, dto.enderecoIds()));
    }
}
