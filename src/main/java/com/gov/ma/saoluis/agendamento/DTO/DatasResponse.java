package com.gov.ma.saoluis.agendamento.DTO;

import java.time.LocalDate;
import java.util.List;

public record DatasResponse(List<LocalDate> datas) {}