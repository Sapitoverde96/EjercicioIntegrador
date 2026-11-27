package com.cesur.valciro.business.models;

/**
 * Estado en el que se encuentra una oferta de empleo.
 */
public enum OfferStatus {
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado"),
    PENDING("Pendiente"),
    INTERVIEW("Entrevista");

    /** Texto que se muestra para el estado. */
    private final String label;

    /**
     * Crea un estado con su texto visible.
     *
     * @param label texto que representa al estado
     */
    OfferStatus(String label) {
        this.label = label;
    }

    /**
     * Devuelve el texto visible del estado.
     *
     * @return etiqueta del estado
     */
    @Override
    public String toString() {
        return label;
    }
}
