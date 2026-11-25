package com.cesur.valciro.business.models;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Interview {
    private Long id;
    private Offer offer;
    private LocalDate date;
    private String notes;
    private InterviewType type;
    private InterviewOutcome outcome;
}