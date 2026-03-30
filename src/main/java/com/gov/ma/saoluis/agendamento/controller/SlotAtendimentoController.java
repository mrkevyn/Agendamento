package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.HorarioDiaResponse;
import com.gov.ma.saoluis.agendamento.DTO.HorarioDisponivelResponse;
import com.gov.ma.saoluis.agendamento.DTO.SlotResponseDTO;
import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.model.SlotAtendimento;
import com.gov.ma.saoluis.agendamento.repository.SlotAtendimentoRepository;
import com.gov.ma.saoluis.agendamento.service.ConfiguracaoAtendimentoService;
import com.gov.ma.saoluis.agendamento.service.SlotAtendimentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
public class SlotAtendimentoController {

    private final SlotAtendimentoService slotService;
    private final SlotAtendimentoRepository slotRepo;
    private final ConfiguracaoAtendimentoService configuracaoService;

    public SlotAtendimentoController(
            SlotAtendimentoService slotService,
            SlotAtendimentoRepository slotRepo,
            ConfiguracaoAtendimentoService configuracaoService
    ) {
        this.slotService = slotService;
        this.slotRepo = slotRepo;
        this.configuracaoService = configuracaoService;
    }

    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarHorariosDisponiveis(
            @RequestParam Long setorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {

        // Busca configuração pelo setor
        ConfiguracaoAtendimento cfg =
                configuracaoService.buscarPorSetorId(setorId);

        if (cfg == null) {
            throw new RuntimeException("Configuração não encontrada para este setor");
        }

        if (cfg.getDatasAtendimento() == null ||
                !cfg.getDatasAtendimento().contains(data)) {
            return ResponseEntity.ok(List.of());
        }

        List<SlotAtendimento> slots =
                slotRepo.findByConfiguracaoIdAndDataOrderByHora(cfg.getId(), data);

        List<HorarioDisponivelResponse> response = slots.stream()
                .filter(SlotAtendimento::temVaga)
                .map(s -> new HorarioDisponivelResponse(
                        s.getHora(),
                        s.getCapacidade() - s.getReservados()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/horarios-do-dia")
    public ResponseEntity<List<HorarioDiaResponse>> listarHorariosDoDia(
            @RequestParam Long setorId,
            @RequestParam Long configuracaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(configuracaoId);

        if (!cfg.getSetor().getId().equals(setorId)) {
            throw new RuntimeException("Configuração não pertence a esta secretaria");
        }

        if (cfg.getDatasAtendimento() == null || !cfg.getDatasAtendimento().contains(data)) {
            return ResponseEntity.ok(List.of());
        }

        //slotService.garantirSlotsDoDia(cfg, data);

        List<SlotAtendimento> slots =
                slotRepo.findByConfiguracaoIdAndDataOrderByHora(configuracaoId, data);

        List<HorarioDiaResponse> response = slots.stream()
                .map(s -> {
                    int vagas = Math.max(0, s.getCapacidade() - s.getReservados());
                    boolean lotado = vagas == 0;
                    return new HorarioDiaResponse(
                            s.getHora(),
                            s.getCapacidade(),
                            s.getReservados(),
                            vagas,
                            lotado
                    );
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/horarios-do-dia")
    public ResponseEntity<Void> excluirHorarioDoDia(
            @RequestParam Long setorId,
            @RequestParam Long configuracaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(configuracaoId);

        if (!cfg.getSetor().getId().equals(setorId)) {
            throw new RuntimeException("Configuração não pertence a este setor");
        }

        slotService.excluirSlot(configuracaoId, data, hora);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar")
    public ResponseEntity<SlotAtendimento> validarSlot(
            @RequestParam Long setorId,
            @RequestParam Long configuracaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(configuracaoId);

        if (!cfg.getSetor().getId().equals(setorId)) {
            throw new RuntimeException("Configuração não pertence a esta secretaria");
        }

        // valida data/hora dentro do bloco e data vinculada
        configuracaoService.validarDisponibilidade(setorId, data, hora);

        // só cria se data é vinculada (garantir já sabe disso)
        //slotService.garantirSlotsDoDia(cfg, data);

        SlotAtendimento slot = slotRepo
                .findByConfiguracaoIdAndDataAndHora(configuracaoId, data, hora)
                .orElseThrow(() -> new RuntimeException("Horário indisponível"));

        if (!slot.temVaga()) {
            throw new RuntimeException("Horário lotado");
        }

        return ResponseEntity.ok(slot);
    }

    @GetMapping("/datas-disponiveis")
    public ResponseEntity<List<LocalDate>> datasDisponiveis(
            @RequestParam Long setorId,
            @RequestParam Long configuracaoId
    ) {
        return ResponseEntity.ok(
                configuracaoService.listarDatasVinculadas(setorId, configuracaoId)
        );
    }

    @GetMapping("/secretaria/{secretariaId}/slots")
    public ResponseEntity<List<SlotAtendimento>> listarSlotsPorSecretaria(
            @PathVariable Long setorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return ResponseEntity.ok(slotService.listarSlotsPorSetor(setorId, data));
    }

    @GetMapping("/slots")
    public List<SlotResponseDTO> buscarSlots(
            @RequestParam Long setorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {

        List<SlotAtendimento> slots =
                slotRepo.findByConfiguracaoSetorIdAndDataAndAtivoTrue(setorId, data);

        return slots.stream()
                .map(slot -> new SlotResponseDTO(
                        slot.getHora(),
                        slot.getCapacidade() - slot.getReservados()
                ))
                .toList();
    }
}
