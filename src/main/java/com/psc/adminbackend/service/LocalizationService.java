package com.psc.adminbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocalizationService {

    @Autowired
    private MessageSource messageSource;

    public String getLocalizedMessage(String code, String languageTag) {
        Locale locale = languageTag != null ? Locale.forLanguageTag(languageTag) : Locale.ENGLISH;
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (Exception e) {
            return messageSource.getMessage(code, null, Locale.ENGLISH);
        }
    }
}