package com.cesur.valciro.business.models;

/**
 * Posible resultado de una entrevista.
 */
public enum InterviewOutcome {
    PENDING("Pendiente"),
    POSITIVE("Positivo"),
    NEGATIVE("Negativo");

    /** Texto que se muestra para el resultado. */
    private final String label;

    /**
     * Crea un resultado con su texto visible.
     *
     * @param label texto que representa al resultado
     */
    InterviewOutcome(String label) {
        this.label = label;
    }

    /**
     * Devuelve el texto visible del resultado.
     *
     * @return etiqueta del resultado
     */
    @Override
    public String toString() {
        return label;
    }
}
