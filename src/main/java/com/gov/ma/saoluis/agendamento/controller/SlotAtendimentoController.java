package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.HorarioDisponivelResponse;
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

    // ✅ 1) Listar horários disponíveis por data
    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarHorariosDisponiveis(
            @RequestParam Long secretariaId,
            @RequestParam Long configuracaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        // 1) pega config
        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(configuracaoId);

        // 2) valida se essa data é permitida (dia da semana) - e janela de horário será aplicada depois
        // aqui você só valida o dia; se quiser validar a janela, não faz sentido sem hora
        // então fazemos só o "dia permitido" implicitamente via garantirSlotsDoDia (ele já checa dia)
        // se você quiser forçar validação do dia, faça um método validarDia(secretariaId, data)

        // 3) garante slots do dia
        slotService.garantirSlotsDoDia(cfg, data);

        // 4) lista slots e filtra os com vaga
        List<SlotAtendimento> slots = slotRepo
                .findByConfiguracaoIdAndDataOrderByHora(configuracaoId, data);

        List<HorarioDisponivelResponse> response = slots.stream()
                .filter(SlotAtendimento::temVaga)
                .map(s -> new HorarioDisponivelResponse(
                        s.getHora(),
                        s.getCapacidade() - s.getReservados()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ✅ 2) Validar um horário específico (uso interno/admin)
    @GetMapping("/validar")
    public ResponseEntity<SlotAtendimento> validarSlot(
            @RequestParam Long secretariaId,
            @RequestParam Long configuracaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        // 1) valida se a config permite esse dia/hora
        configuracaoService.validarDisponibilidade(secretariaId, data, hora);

        // 2) garante slots do dia
        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(configuracaoId);
        slotService.garantirSlotsDoDia(cfg, data);

        // 3) busca o slot (sem lock aqui, pq é validação; lock é no AGENDAR)
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
            @RequestParam Long secretariaId,
            @RequestParam Long configuracaoId,
            @RequestParam(defaultValue = "30") int dias
    ) {
        return ResponseEntity.ok(
                configuracaoService.listarDatasDisponiveis(secretariaId, configuracaoId, dias)
        );
    }

    @GetMapping("/secretaria/{secretariaId}/slots")
    public ResponseEntity<List<SlotAtendimento>> listarSlotsPorSecretaria(
            @PathVariable Long secretariaId,
            @RequestParam(required = false) LocalDate data
    ) {
        return ResponseEntity.ok(slotService.listarSlotsPorSecretaria(secretariaId, data));
    }
}
