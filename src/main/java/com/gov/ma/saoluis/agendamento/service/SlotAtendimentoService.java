package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.model.DiaSemana;
import com.gov.ma.saoluis.agendamento.model.HorarioAtendimento;
import com.gov.ma.saoluis.agendamento.model.SlotAtendimento;
import com.gov.ma.saoluis.agendamento.repository.SlotAtendimentoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SlotAtendimentoService {

    private final SlotAtendimentoRepository slotRepo;

    public SlotAtendimentoService(SlotAtendimentoRepository slotRepo) {
        this.slotRepo = slotRepo;
    }

    @Transactional
    public void garantirSlotsDoDia(ConfiguracaoAtendimento cfg, LocalDate data) {

        // 📆 só gera slot se a data estiver configurada
        if (cfg.getDatasAtendimento() == null
                || !cfg.getDatasAtendimento().contains(data)) {
            return;
        }

        for (HorarioAtendimento ht : cfg.getHorarios()) {
            // ✅ ideal: insertIfNotExists (ON CONFLICT) para não abortar transação
            slotRepo.insertIfNotExists(
                    cfg.getId(),
                    data,
                    ht.getHora(),
                    cfg.getNumeroGuiches()
            );
        }
    }

    @Transactional
    public SlotAtendimento lockSlot(Long configuracaoId, LocalDate data, LocalTime hora) {
        return slotRepo.lockSlot(configuracaoId, data, hora)
                .orElseThrow(() -> new RuntimeException("Horário indisponível"));
    }

    public List<SlotAtendimento> listarHorariosDisponiveis(Long configuracaoId, LocalDate data) {
        return slotRepo.findByConfiguracaoIdAndDataOrderByHora(configuracaoId, data)
                .stream()
                .filter(SlotAtendimento::temVaga)
                .toList();
    }

    private DiaSemana converterDia(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    public List<SlotAtendimento> listarSlotsPorSecretaria(Long secretariaId, LocalDate data) {
        if (data != null) {
            return slotRepo.findByConfiguracaoSecretariaIdAndAtivoTrueAndDataOrderByHoraAsc(secretariaId, data);
        }
        // exemplo: hoje (ou os próximos 7 dias, se preferir)
        return slotRepo.findByConfiguracaoSecretariaIdAndAtivoTrueAndDataGreaterThanEqualOrderByDataAscHoraAsc(
                secretariaId, LocalDate.now()
        );
    }
}
