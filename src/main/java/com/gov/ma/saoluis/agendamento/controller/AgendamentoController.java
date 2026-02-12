package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoAppRequest;
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

    @GetMapping("/enderecos/{enderecoId}")
    public ResponseEntity<List<AgendamentoDTO>> listarPorEndereco(@PathVariable Long enderecoId) {
        return ResponseEntity.ok(agendamentoService.listarPorEnderecoGerenciador(enderecoId));
    }

    @GetMapping("/{id}")
    public Agendamento buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id);
    }

    // 🔹 Listar todos os agendamentos com detalhes
    @GetMapping("/detalhamento")
    public ResponseEntity<List<AgendamentoDTO>> listarTodosComDetalhes() {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarTodosComDetalhes();
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/detalhes/{agendamentoId}")
    public ResponseEntity<List<AgendamentoDTO>> listarComDetalhes(@PathVariable Long agendamentoId) {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarAgendamentosComDetalhes(agendamentoId);
        return ResponseEntity.ok(agendamentos);
    }

    @PostMapping("/agendar-app")
    public Agendamento criarApp(@RequestBody AgendamentoAppRequest req) {
        return agendamentoService.salvarApp(req);
    }

    @PostMapping("/espontaneo/{secretariaId}")
    public ResponseEntity<Agendamento> criarEspontaneo(
            @PathVariable Long secretariaId,
            @RequestBody Agendamento agendamento
    ) {
        Agendamento salvo = agendamentoService.criarEspontaneo(secretariaId, agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public Agendamento atualizar(@PathVariable Long id, @RequestBody Agendamento novosDados) {
        return agendamentoService.atualizar(id, novosDados);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
    }

    @PostMapping("/chamar/normal/{enderecoId}/{gerenciadorId}")
    public ResponseEntity<Agendamento> chamarProximaNormal(
            @PathVariable Long enderecoId,
            @PathVariable Long gerenciadorId
    ) {
        return ResponseEntity.ok(
                agendamentoService.chamarProximaNormal(enderecoId, gerenciadorId)
        );
    }

    @PostMapping("/chamar/prioridade/{enderecoId}/{gerenciadorId}")
    public ResponseEntity<Agendamento> chamarProximaPrioridade(
            @PathVariable Long enderecoId,
            @PathVariable Long gerenciadorId
    ) {
        return ResponseEntity.ok(
                agendamentoService.chamarProximaPrioridade(enderecoId, gerenciadorId)
        );
    }

    @PostMapping("/chamar/por-senha/{senha}/{atendenteId}")
    public ResponseEntity<AgendamentoResponseDTO> chamarPorSenha(
            @PathVariable String senha,
            @PathVariable Long atendenteId
    ) throws Exception {
        return ResponseEntity.ok(
                agendamentoService.chamarPorSenha(senha, atendenteId)
        );
    }

    @GetMapping("/ultimas-chamadas/{enderecoId}")
    public ResponseEntity<List<UltimaChamadaDTO>> buscarUltimasChamadas(
            @PathVariable Long enderecoId) {

        return ResponseEntity.ok(
                agendamentoService.getUltimasChamadasPorSecretaria(enderecoId)
        );
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
