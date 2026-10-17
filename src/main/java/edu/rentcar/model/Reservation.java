package edu.rentcar.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Result of a reservation attempt, with its total cost, final status and the reason of that status.
 * It replaces the placeholder Reservation of the starter project.
 */
public class Reservation {
    private final String id;
    private final RentalRequest request;
    private final double totalCost;
    private final ReservationStatus status;
    private final String reason;

    public Reservation(String id, RentalRequest request, double totalCost,
                       ReservationStatus status, String reason) {
        this.id = Objects.requireNonNull(id, "id");
        this.request = Objects.requireNonNull(request, "request");
        this.totalCost = totalCost;
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason == null ? "" : reason;
    }

    public String getId() {
        return id;
    }

    public RentalRequest getRequest() {
        return request;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    /** Explains why the reservation has this status, to show it to the user. */
    public String getReason() {
        return reason;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    // Convenience getters, used by the reservations table of the interface.

    public String getCustomerId() {
        return request.getCustomer().getId();
    }

    public String getVehicleId() {
        return request.getVehicle().getId();
    }

    public LocalDate getStartDate() {
        return request.getStartDate();
    }

    public LocalDate getEndDate() {
        return request.getEndDate();
    }
}