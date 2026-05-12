package br.com.sistemamoedas.service;

import java.time.LocalDate;

public final class SemestreUtil {

    private SemestreUtil() {
    }

    public static String atual() {
        LocalDate hoje = LocalDate.now();
        int semestre = hoje.getMonthValue() <= 6 ? 1 : 2;
        return hoje.getYear() + "-" + semestre;
    }
}
