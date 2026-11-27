package com.cesur.valciro.business.models;

/**
 * Tipo de entrevista según la forma en la que se realiza.
 */
public enum InterviewType {
    TELEPHONE("Telefónica"),
    TECHNICAL("Técnica"),
    ONSITE("Presencial"),
    ONLINE("Online");

    /** Texto que se muestra para el tipo de entrevista. */
    private final String label;

    /**
     * Crea un tipo de entrevista con su texto visible.
     *
     * @param label texto que representa al tipo
     */
    InterviewType(String label) {
        this.label = label;
    }

    /**
     * Devuelve el texto visible del tipo de entrevista.
     *
     * @return tipo de entrevista
     */
    @Override
    public String toString() {
        return label;
    }
}
