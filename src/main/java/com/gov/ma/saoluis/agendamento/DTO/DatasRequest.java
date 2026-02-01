package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDate;
import java.util.Set;

public record DatasRequest(Set<LocalDate> datas) {}