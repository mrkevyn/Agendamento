package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.LogSistema;
import com.gov.ma.saoluis.agendamento.repository.LogSistemaRepository;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogSistemaRepository logRepository;

    public LogService(LogSistemaRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void registrar(
            Long usuarioId,
            String tipoUsuario,
            String acao,
            String metadado
    ) {
        LogSistema log = new LogSistema();
        log.setUsuarioId(usuarioId);
        log.setTipoUsuario(tipoUsuario);
        log.setAcao(acao);
        log.setMetadado(metadado);

        logRepository.save(log);
    }
}
