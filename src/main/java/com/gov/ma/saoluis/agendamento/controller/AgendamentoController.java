package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.service.AgendamentoService;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<AgendamentoDTO>> listarPorSetor(
            @PathVariable Long setorId,
            @RequestParam(required = false) Long gerenciadorId,

            @RequestParam(name = "perfil", defaultValue = "ATENDENTE") String perfil
    ) {
        return ResponseEntity.ok(
                agendamentoService.listarPorSetorGerenciador(setorId, gerenciadorId, perfil)
        );
    }

    @GetMapping("/{id}")
    public Agendamento buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id);
    }

    // Listar todos os agendamentos com detalhes
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

    @PostMapping("/externo")
    public ResponseEntity<Map<String, String>> criarAgendamentoExterno(
            @RequestBody @Valid AgendamentoExternoRequest request
    ) {
        Agendamento agendamento = agendamentoService.salvarExterno(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("senha", agendamento.getSenha()));
    }

    @PostMapping("/espontaneo/{secretariaId}")
    public ResponseEntity<AgendamentoUpdateResponseDTO> criarEspontaneo(
            @PathVariable Long secretariaId,
            @RequestBody AgendamentoEspontaneoDTO dto
    ) {
        // 1. O service processa a regra de negócio e salva
        Agendamento salvo = agendamentoService.criarEspontaneo(secretariaId, dto);

        // 2. Mapeamento Manual para o DTO de Resposta
        // Isso blinda o Jackson contra os Proxies do Hibernate
        AgendamentoUpdateResponseDTO response = new AgendamentoUpdateResponseDTO(
                salvo.getId(),
                salvo.getNomeCidadao(),
                salvo.getServico() != null ? salvo.getServico().getId() : null,
                salvo.getServico() != null ? salvo.getServico().getNome() : "Não informado",
                salvo.getSetor() != null ? salvo.getSetor().getId() : null,
                salvo.getSenha(),
                salvo.getSituacao() != null ? salvo.getSituacao().name() : "AGENDADO",
                salvo.getTipoAtendimento() != null ? salvo.getTipoAtendimento().getNome() : "NORMAL",
                salvo.getObservacao()
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

    @PostMapping("/chamar/prioridade/{setorId}/{gerenciadorId}")
    public ResponseEntity<?> chamarProximaPrioridade(
            @PathVariable Long setorId,
            @PathVariable Long gerenciadorId
    ) {
        try {
            Agendamento agendamento = agendamentoService.chamarProximaPrioridade(setorId, gerenciadorId);
            return ResponseEntity.ok(Map.of(
                    "id", agendamento.getId(),
                    "senha", agendamento.getSenha(),
                    "sucesso", true
            ));
        } catch (RuntimeException e) {
            // Devolve 200 amigável para fila vazia
            if ("Fila vazia".equals(e.getMessage())) {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Não há prioridades na fila para hoje."));
            }
            // Devolve 200 amigável informando que ele está ocupado (Bloqueio do Backend!)
            if ("Atendente ocupado".equals(e.getMessage())) {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Ação bloqueada: Você já possui um atendimento em aberto. Finalize-o primeiro."));
            }

            // Se for outro erro, manda um erro 400 real
            return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem", e.getMessage()));
        }
    }

    // Faça a mesma coisa para o chamarNormal:
    @PostMapping("/chamar/normal/{setorId}/{gerenciadorId}")
    public ResponseEntity<?> chamarNormal(
            @PathVariable Long setorId,
            @PathVariable Long gerenciadorId
    ) {
        try {
            Agendamento agendamento = agendamentoService.chamarProximaNormal(setorId, gerenciadorId);
            return ResponseEntity.ok(Map.of(
                    "id", agendamento.getId(),
                    "senha", agendamento.getSenha(),
                    "sucesso", true
            ));
        } catch (RuntimeException e) {
            if ("Fila vazia".equals(e.getMessage())) {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Não há pacientes aguardando na fila de atendimento NORMAL."));
            }
            if ("Atendente ocupado".equals(e.getMessage())) {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Ação bloqueada: Você já possui um atendimento em aberto. Finalize-o primeiro."));
            }
            return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem", e.getMessage()));
        }
    }

    @PostMapping("/chamar/por-senha/{senha}/{atendenteId}/{setorId}")
    public ResponseEntity<?> chamarPorSenha(
            @PathVariable String senha,
            @PathVariable Long atendenteId,
            @PathVariable Long setorId
    ) throws Exception {
        try {
            // Aqui mantemos o seu DTO
            AgendamentoResponseDTO dto = agendamentoService.chamarPorSenha(senha, atendenteId, setorId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            // Para o chamarPorSenha, como o Vue usa o e.response.data.mensagem no CATCH,
            // retornamos Bad Request (400) com a mensagem limpa
            if ("Atendente ocupado".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Ação bloqueada: Você já possui um atendimento em aberto."));
            }
            throw e; // Repassa para o GlobalExceptionHandler se for outro erro
        }
    }

    @GetMapping("/ultimas-chamadas/{setorId}")
    public ResponseEntity<List<UltimaChamadaDTO>> buscarUltimasChamadas(
            @PathVariable Long setorId) {

        // Alterado para buscar as últimas chamadas do setor específico
        return ResponseEntity.ok(
                agendamentoService.getUltimasChamadasPorSetor(setorId)
        );
    }

    @PutMapping("/finalizar/{id}")
    public ResponseEntity<?> finalizar(@PathVariable Long id) {
        agendamentoService.finalizarAtendimento(id);
        // Retorne apenas uma mensagem de sucesso
        return ResponseEntity.ok(Map.of("mensagem", "Atendimento finalizado com sucesso"));
    }

    @PutMapping("/cancelar/{id}")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        agendamentoService.cancelarAtendimento(id);
        // Retorne apenas uma mensagem de sucesso
        return ResponseEntity.ok(Map.of("mensagem", "Atendimento cancelado com sucesso"));
    }

    @GetMapping("/historico/{cpf}")
    public ResponseEntity<List<HistoricoDTO>> consultarHistorico(@PathVariable String cpf) {
        List<HistoricoDTO> historico = agendamentoService.buscarHistorico(cpf);

        if (historico.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historico);
    }

    @PutMapping("/heartbeat/{agendamentoId}")
    public ResponseEntity<?> heartbeat(@PathVariable Long agendamentoId) {
        agendamentoService.atualizarPing(agendamentoId);
        return ResponseEntity.ok(Map.of("mensagem", "Ping recebido"));
    }
}
