package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.service.AgendamentoService;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")

public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/listar-todos")
    public List<AgendamentoDTO> listarTodos() {
        return agendamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Agendamento buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id);
    }
    
    @GetMapping("/detalhes")
    public ResponseEntity<List<AgendamentoDTO>> listarComDetalhes() {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarAgendamentosComDetalhes();
        return ResponseEntity.ok(agendamentos);
    }

    @PostMapping
    public Agendamento criar(@RequestBody Agendamento agendamento) {
        agendamento.setId(null); // garante que será um novo agendamento
        return agendamentoService.salvar(agendamento);
    }

    @PutMapping("/{id}")
    public Agendamento atualizar(@PathVariable Long id, @RequestBody Agendamento novosDados) {
        return agendamentoService.atualizar(id, novosDados);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
    }

    @PostMapping("/chamar/normal")
    public ResponseEntity<Agendamento> chamarProximaNormal() {
        return ResponseEntity.ok(agendamentoService.chamarProximaNormal());
    }

    @PostMapping("/chamar/prioridade")
    public ResponseEntity<Agendamento> chamarProximaPrioridade() {
        return ResponseEntity.ok(agendamentoService.chamarProximaPrioridade());
    }

    @GetMapping("/ultima-chamada")
    public ResponseEntity<UltimaChamadaDTO> getUltimaChamada() {
        UltimaChamadaDTO ultima = agendamentoService.getUltimaChamada();

        if (ultima == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ultima);
    }

    @PutMapping("/finalizar/{id}")
    public ResponseEntity<Agendamento> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.finalizarAtendimento(id));
    }

    @PutMapping("/cancelar/{id}")
    public ResponseEntity<Agendamento> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.cancelarAtendimento(id));
    }
}
