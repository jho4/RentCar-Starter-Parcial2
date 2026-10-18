package edu.rentcar.extras;

/**
 * Common abstraction for an individual service (GPS, child seat, ...) and for a bundle of services.
 * It is the base of both the Decorator (RF-005) and the Composite (RF-007) patterns.
 */
public interface ServiceComponent {

    /** Human readable description of the service or bundle. */
    String getDescription();

    /**
     * Cost of this service or bundle for a rental of the given number of days.
     * Each implementation decides if it charges per day or a fixed amount.
     */
    double getCost(long days);
}