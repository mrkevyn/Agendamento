package com.gov.ma.saoluis.agendamento.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {
    public static final ZoneId ZONE_FORTALEZA = ZoneId.of("America/Fortaleza");

    public static LocalDateTime agoraFortaleza() {
        return LocalDateTime.now(ZONE_FORTALEZA);
    }

    public static LocalDate hojeFortaleza() {
        return LocalDate.now(ZONE_FORTALEZA);
    }
}
