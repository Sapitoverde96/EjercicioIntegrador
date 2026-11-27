package com.cesur.valciro.business.models;

import lombok.Data;
import java.time.LocalDate;

/**
 * Datos de una entrevista asociada a una oferta.
 */
@Data
public class Interview {
    private Long id;
    private Offer offer;
    private LocalDate date;
    private String notes;
    private InterviewType type;
    private InterviewOutcome outcome;
}