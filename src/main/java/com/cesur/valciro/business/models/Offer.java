package com.cesur.valciro.business.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Offer {
    private Long id;
    private String title;
    private Company company;
    private OfferStatus status;
    private LocalDate date;
    private String link;
    private String description;
    private String notes;
    private Double salary;
}
