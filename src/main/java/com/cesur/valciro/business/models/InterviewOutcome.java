package com.cesur.valciro.business.models;

public enum InterviewOutcome {
    PENDING("Pendiente"),
    POSITIVE("Positivo"),
    NEGATIVE("Negativo");

    private final String label;

    InterviewOutcome(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
