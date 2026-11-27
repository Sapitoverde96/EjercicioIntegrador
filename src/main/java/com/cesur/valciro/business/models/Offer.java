package com.cesur.valciro.business.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Datos de una oferta de empleo publicada por una empresa.
 */
@Data
public class Offer {
    private Long id;
    private String title;
    private Company company;
    private OfferStatus status;
    private LocalDate date;
    /** Enlace a la oferta original. */
    private String link;
    private String description;
    /** Notas añadidas sobre la oferta. */
    private String notes;
    /** Salario ofrecido. */
    private Double salary;
}
