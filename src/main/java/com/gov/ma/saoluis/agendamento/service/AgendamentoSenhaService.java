package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AgendamentoSenhaService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoSenhaService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Agendamento salvarComSenhaUnica(Agendamento agendamento, Long configId, LocalDate data) {

        for (int tentativas = 1; tentativas <= 5; tentativas++) {

            agendamento.setSenha(gerarSenhaParaDia(configId, agendamento.getTipoAtendimento(), data));

            try {
                return agendamentoRepository.saveAndFlush(agendamento);
            } catch (DataIntegrityViolationException e) {
                if (tentativas == 5) {
                    throw new RuntimeException("Falha ao gerar senha única");
                }
            }
        }

        throw new RuntimeException("Falha ao gerar senha única");
    }

    private String gerarSenhaParaDia(Long configId, String tipo, LocalDate data) {
        String prefixo = gerarPrefixo(tipo);

        String ultima = agendamentoRepository.findUltimaSenhaDoDia(configId, tipo, data);

        if (ultima == null) return prefixo + "001";

        return gerarProximaSenha(ultima);
    }

    private String gerarProximaSenha(String senhaAntiga) {
        if (senhaAntiga == null || senhaAntiga.length() < 2) return "N001";

        String prefixo = senhaAntiga.substring(0, 1);
        int numero = Integer.parseInt(senhaAntiga.substring(1));

        return String.format("%s%03d", prefixo, numero + 1);
    }

    private String gerarPrefixo(String tipo) {
        if (tipo == null) return "N";
        if ("PRIORIDADE".equalsIgnoreCase(tipo)) return "P";
        // se tiver PREFERENCIAL, etc, ajuste aqui
        return "N";
    }
}
