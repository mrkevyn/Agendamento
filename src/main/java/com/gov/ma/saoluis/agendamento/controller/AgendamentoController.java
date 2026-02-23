package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.*;
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

    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<AgendamentoDTO>> listarPorSetor(@PathVariable Long setorId) {
        return ResponseEntity.ok(agendamentoService.listarPorSetorGerenciador(setorId));
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
    public ResponseEntity<AgendamentoUpdateResponseDTO> criarEspontaneo(
            @PathVariable Long secretariaId,
            @RequestBody AgendamentoEspontaneoDTO dto // ✅ Agora recebe o DTO
    ) {
        // Envia o DTO para o service
        Agendamento salvo = agendamentoService.criarEspontaneo(secretariaId, dto);

        // Mapeia para o DTO de Resposta (usando o construtor do seu Record)
        AgendamentoUpdateResponseDTO response = new AgendamentoUpdateResponseDTO(
                salvo.getId(),
                salvo.getNomeCidadao(),
                salvo.getServico().getId(),
                salvo.getServico().getNome(),
                salvo.getSetor().getId(),
                salvo.getSenha(),
                salvo.getSituacao().name(),
                salvo.getTipoAtendimento()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/atualizar-espontaneo/{id}")
    public ResponseEntity<AgendamentoUpdateResponseDTO> atualizarEspontaneo(
            @PathVariable Long id,
            @RequestBody AgendamentoUpdateDTO dto
    ) {
        return ResponseEntity.ok(agendamentoService.atualizarEspontaneo(id, dto));
    }

    @PutMapping("/{id}")
    public Agendamento atualizar(@PathVariable Long id, @RequestBody Agendamento novosDados) {
        return agendamentoService.atualizar(id, novosDados);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
    }

    @PostMapping("/chamar/normal/{setorId}/{gerenciadorId}")
    public ResponseEntity<Agendamento> chamarProximaNormal(
            @PathVariable Long setorId,
            @PathVariable Long gerenciadorId
    ) {
        return ResponseEntity.ok(
                agendamentoService.chamarProximaNormal(setorId, gerenciadorId)
        );
    }

    @PostMapping("/chamar/prioridade/{setorId}/{gerenciadorId}")
    public ResponseEntity<Agendamento> chamarProximaPrioridade(
            @PathVariable Long setorId,
            @PathVariable Long gerenciadorId
    ) {
        // ✅ Alterado de enderecoId para setorId
        return ResponseEntity.ok(
                agendamentoService.chamarProximaPrioridade(setorId, gerenciadorId)
        );
    }

    @PostMapping("/chamar/por-senha/{senha}/{atendenteId}/{setorId}")
    public ResponseEntity<AgendamentoResponseDTO> chamarPorSenha(
            @PathVariable String senha,
            @PathVariable Long atendenteId,
            @PathVariable Long setorId
    ) throws Exception {
        // ✅ Adicionado setorId no Path pois o atendente agora tem múltiplos setores
        return ResponseEntity.ok(
                agendamentoService.chamarPorSenha(senha, atendenteId, setorId)
        );
    }

    @GetMapping("/ultimas-chamadas/{setorId}")
    public ResponseEntity<List<UltimaChamadaDTO>> buscarUltimasChamadas(
            @PathVariable Long setorId) {

        // ✅ Alterado para buscar as últimas chamadas do setor específico
        return ResponseEntity.ok(
                agendamentoService.getUltimasChamadasPorSetor(setorId)
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
