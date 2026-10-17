package edu.rentcar.model;

/**
 * Final state of a reservation attempt.
 *
 * CONFIRMED: valid request, vehicle available, customer authorized and payment approved.
 * PENDING:   the payment could not be completed because of a technical problem
 *            (payment API down, HTTP 503, verification service error). It can be retried.
 * FAILED:    invalid request, vehicle not available, customer not authorized or payment declined.
 *
 * Only CONFIRMED reservations block the availability of a vehicle.
 */
public enum ReservationStatus {
    CONFIRMED,
    PENDING,
    FAILED
}