package com.gestaoescolar.config;

import com.vaadin.flow.component.datepicker.DatePicker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class BrasilConfig {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("pt", "BR"));
        return slr;
    }

    // Configura o DatePicker para usar o locale brasileiro - VERSÃO CORRIGIDA
    @Bean
    public DatePicker.DatePickerI18n datePickerI18n() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        // CORREÇÃO: Usar List.of()
        i18n.setMonthNames(List.of(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        ));

        i18n.setWeekdays(List.of(
                "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
                "Quinta-feira", "Sexta-feira", "Sábado"
        ));

        i18n.setWeekdaysShort(List.of(
                "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"
        ));

        i18n.setToday("Hoje");
        i18n.setCancel("Cancelar");
        i18n.setFirstDayOfWeek(1); // Segunda-feira
        return i18n;
    }
}