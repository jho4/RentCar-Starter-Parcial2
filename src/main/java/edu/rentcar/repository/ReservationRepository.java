package edu.rentcar.repository;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory repository for managing reservations and checking vehicle availability.
 */
public class ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();
    private int nextId = 1001;

    public synchronized Reservation save(RentalRequest request, double totalCost,
                                         ReservationStatus status, String reason) {
        String id = "RES-" + nextId++;
        Reservation reservation = new Reservation(id, request, totalCost, status, reason);
        reservations.add(reservation);
        return reservation;
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    public boolean isVehicleAvailable(String vehicleId, LocalDate start, LocalDate end) {
        for (Reservation r : reservations) {
            // Only confirmed reservations block vehicle availability
            if (r.isConfirmed() && r.getVehicleId().equals(vehicleId)) {
                LocalDate rStart = r.getStartDate();
                LocalDate rEnd = r.getEndDate();
                // Date overlap check: start < existingEnd AND end > existingStart
                if (start.isBefore(rEnd) && end.isAfter(rStart)) {
                    return false;
                }
            }
        }
        return true;
    }
}