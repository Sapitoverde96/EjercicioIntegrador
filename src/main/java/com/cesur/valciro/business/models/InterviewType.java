package com.cesur.valciro.business.models;

public enum InterviewType {
    TELEPHONE("Telefónica"),
    TECHNICAL("Técnica"),
    ONSITE("Presencial"),
    ONLINE("Online");

    private final String label;

    InterviewType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
