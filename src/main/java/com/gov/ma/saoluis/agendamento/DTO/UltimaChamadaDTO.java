package com.gov.ma.saoluis.agendamento.DTO;

import java.sql.Timestamp;

public interface UltimaChamadaDTO {

    Long getAgendamentoId();
    String getSenha();
    String getTipoAtendimento();
    Timestamp getHoraChamada();

    Long getUsuarioId();
    String getUsuarioNome();

    Long getServicoId();
    String getServicoNome();

    Integer getGuiche();
}
