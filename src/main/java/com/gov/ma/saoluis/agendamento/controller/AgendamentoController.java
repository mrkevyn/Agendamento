package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.service.AgendamentoService;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;

import org.springframework.http.HttpStatus;
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

    @GetMapping("/secretaria/{secretariaId}")
    public ResponseEntity<List<AgendamentoDTO>> listarPorSecretaria(@PathVariable Long secretariaId) {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarPorSecretaria(secretariaId);
        return ResponseEntity.ok(agendamentos);
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

    @PostMapping("/chamar/normal/{secretariaId}")
    public ResponseEntity<Agendamento> chamarProximaNormal(
            @PathVariable Long secretariaId
    ) {
        return ResponseEntity.ok(agendamentoService.chamarProximaNormal(secretariaId));
    }

    @PostMapping("/chamar/prioridade/{secretariaId}")
    public ResponseEntity<Agendamento> chamarProximaPrioridade(
            @PathVariable Long secretariaId
    ) {
        return ResponseEntity.ok(agendamentoService.chamarProximaPrioridade(secretariaId));
    }

    @PostMapping("/chamar/por-senha/{senha}")
    public ResponseEntity<AgendamentoResponseDTO> chamarPorSenha(@PathVariable String senha) {
        try {
            AgendamentoResponseDTO agendamento = agendamentoService.chamarPorSenha(senha);
            return ResponseEntity.ok(agendamento);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/ultima-chamada")
    public ResponseEntity<UltimaChamadaDTO> getUltimaChamada() {
        UltimaChamadaDTO ultima = agendamentoService.getUltimaChamada();

        if (ultima == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ultima);
    }

    @PutMapping("/finalizar/{id}/{atendenteId}")
    public ResponseEntity<Agendamento> finalizar(
            @PathVariable Long id,
            @PathVariable Long atendenteId
    ) {
        return ResponseEntity.ok(agendamentoService.finalizarAtendimento(id, atendenteId));
    }

    @PutMapping("/cancelar/{id}")
    public ResponseEntity<Agendamento> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.cancelarAtendimento(id));
    }
}
