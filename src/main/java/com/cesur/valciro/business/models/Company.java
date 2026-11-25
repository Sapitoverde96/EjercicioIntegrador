package com.cesur.valciro.business.models;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Company {
    private Long id;
    private String name;
    private String industry;
    private String website;
    private LocalDate date;
}