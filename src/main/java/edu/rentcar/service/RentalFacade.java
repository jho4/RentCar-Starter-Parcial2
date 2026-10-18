package edu.rentcar.service;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.model.Vehicle;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;
import edu.rentcar.payment.PaymentStatus;
import edu.rentcar.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade pattern (RF-002): coordinates the complete rental workflow, including request validation,
 * vehicle availability check, total cost calculation (plans and services),
 * payment processing, and reservation registration.
 */
public class RentalFacade {
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public RentalFacade(ReservationRepository reservationRepository, PaymentGateway paymentGateway) {
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    /** All reservations registered during this execution (confirmed, pending and failed). */
    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    /** Availability of a vehicle for a period; only confirmed reservations block it. */
    public boolean isVehicleAvailable(Vehicle vehicle, LocalDate start, LocalDate end) {
        return reservationRepository.isVehicleAvailable(vehicle.getId(), start, end);
    }

    public Reservation processRental(RentalRequest request) {
        // 1. Validate date coherence
        if (request.getStartDate().isAfter(request.getEndDate()) ||
                request.getStartDate().isEqual(request.getEndDate())) {
            return reservationRepository.save(request, 0.0, ReservationStatus.FAILED,
                    "Start date must be before end date.");
        }

        // 2. Check vehicle availability
        boolean available = reservationRepository.isVehicleAvailable(
                request.getVehicle().getId(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (!available) {
            return reservationRepository.save(request, 0.0, ReservationStatus.FAILED,
                    "Vehicle is not available for the selected period.");
        }

        // 3. Calculate total cost (base plan + optional services per rental days)
        long days = request.getRentalDays();
        double baseCost = request.getPlan().calculateBaseCost(request.getVehicle(), days);
        double servicesCost = request.getServices().getCost(days);
        double totalCost = baseCost + servicesCost;

        // 4. Execute verification and payment workflow (Proxy + Adapter)
        PaymentRequest paymentRequest = new PaymentRequest(
                "RES-PENDING",
                request.getCustomer().getId(),
                totalCost,
                request.getPaymentToken()
        );

        PaymentResult paymentResult = paymentGateway.pay(paymentRequest);

        // 5. Determine reservation status
        ReservationStatus status;
        String reason;

        if (paymentResult.isApproved()) {
            status = ReservationStatus.CONFIRMED;
            reason = "Payment approved: " + paymentResult.message();
        } else if (paymentResult.status() == PaymentStatus.TECHNICAL_ERROR) {
            status = ReservationStatus.PENDING;
            reason = "Payment pending due to technical error: " + paymentResult.message();
        } else {
            status = ReservationStatus.FAILED;
            reason = "Payment declined: " + paymentResult.message();
        }

        // 6. Save and return the resulting reservation
        return reservationRepository.save(request, totalCost, status, reason);
    }
}