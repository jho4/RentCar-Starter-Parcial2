package edu.rentcar.service;

import edu.rentcar.builder.RentalRequestBuilder;
import edu.rentcar.extras.ExtraOption;
import edu.rentcar.extras.ExtrasAssembler;
import edu.rentcar.factory.PremiumPlanFactory;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.model.Vehicle;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentResult;
import edu.rentcar.repository.ReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalFacadeStatusTest {

    private final Customer customer = new Customer("CUS-1", "Alice", "alice@test.com", true);
    private final Vehicle vehicle = new Vehicle("VEH-1", "Toyota", "Corolla", "Economy", 2024, 100000.0, "Available");
    private final LocalDate start = LocalDate.of(2026, 12, 1);
    private final LocalDate end = LocalDate.of(2026, 12, 4);

    private RentalRequest request() {
        return new RentalRequestBuilder()
                .customer(customer)
                .vehicle(vehicle)
                .dates(start, end)
                .plan(new PremiumPlanFactory().createPlan())
                .services(ExtrasAssembler.assemble(EnumSet.of(ExtraOption.GPS)))
                .paymentToken("tok_approved")
                .build();
    }

    @Test
    void totalCostReflectsPlanAndExtras() {
        RentalFacade facade = new RentalFacade(new ReservationRepository(), r -> PaymentResult.approved("OK"));

        Reservation reservation = facade.processRental(request());

        // Premium: 100000*3*1.35 + 20 = 405020, GPS: 5*3 = 15
        assertEquals(405035.0, reservation.getTotalCost(), 0.001);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void declinedPaymentIsFailedAndDoesNotBlockTheVehicle() {
        PaymentGateway declining = r -> PaymentResult.declined("NOT_APPROVED");
        RentalFacade facade = new RentalFacade(new ReservationRepository(), declining);

        Reservation reservation = facade.processRental(request());

        assertEquals(ReservationStatus.FAILED, reservation.getStatus());
        assertFalse(reservation.isConfirmed());
        assertTrue(facade.isVehicleAvailable(vehicle, start, end));
        assertEquals(1, facade.getReservations().size());
    }

    @Test
    void technicalPaymentErrorLeavesTheReservationPending() {
        RentalFacade facade = new RentalFacade(new ReservationRepository(), r -> PaymentResult.technicalError("HTTP 503"));

        Reservation reservation = facade.processRental(request());

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertFalse(reservation.isConfirmed());
    }

    @Test
    void confirmedReservationBlocksTheVehicle() {
        RentalFacade facade = new RentalFacade(new ReservationRepository(), r -> PaymentResult.approved("OK"));

        facade.processRental(request());

        assertFalse(facade.isVehicleAvailable(vehicle, start.plusDays(1), end.plusDays(1)));
        assertTrue(facade.isVehicleAvailable(vehicle, end, end.plusDays(2)));
    }
}