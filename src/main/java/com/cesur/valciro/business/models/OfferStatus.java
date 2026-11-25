package com.cesur.valciro.business.models;

public enum OfferStatus {
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado"),
    PENDING("Pendiente"),
    INTERVIEW("Entrevista");

    private final String label;

    OfferStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
