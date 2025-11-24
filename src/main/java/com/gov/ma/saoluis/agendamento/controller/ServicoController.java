package com.gov.ma.saoluis.agendamento.controller;

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

    // Exemplo de teste rápido com System.out.print
    @GetMapping("/teste")
    public ResponseEntity<String> testeSelect() {
        List<Servico> servicos = servicoService.listarTodos();
        servicos.forEach(s -> System.out.println("ID: " + s.getId() + ", Nome: " + s.getNome()));
        return ResponseEntity.ok("Teste executado, veja o console!");
    }
}
