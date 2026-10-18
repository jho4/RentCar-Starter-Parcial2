package edu.rentcar.service;

import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.factory.RentalPlan;
import edu.rentcar.model.*;
import edu.rentcar.payment.*;
import edu.rentcar.proxy.CustomerVerificationProxy;
import edu.rentcar.proxy.CustomerVerifier;
import edu.rentcar.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalFacadeTest {

    private ReservationRepository repository;
    private RentalFacade facade;
    private Customer authorizedCustomer;
    private Customer unauthorizedCustomer;
    private Vehicle vehicle;
    private RentalPlan plan;
    private ServiceComponent services;

    @BeforeEach
    void setUp() {
        repository = new ReservationRepository();

        authorizedCustomer = new Customer("CUS-1", "Alice", "alice@test.com", true);
        unauthorizedCustomer = new Customer("CUS-2", "Bob", "bob@test.com", false);
        vehicle = new Vehicle("VEH-1", "Toyota", "Corolla", "Economy", 2024, 100000.0, "Available");

        plan = new RentalPlan() {
            @Override public String getName() { return "Test Plan"; }
            @Override public String getCoverage() { return "Full"; }
            @Override public String getAssistance() { return "24/7"; }
            @Override public double calculateBaseCost(Vehicle v, long days) { return v.getDailyRate() * days; }
        };

        services = new ServiceComponent() {
            @Override public String getDescription() { return "GPS"; }
            @Override public double getCost(long days) { return 20000.0 * days; }
        };

        // Mock gateway returning APPROVED
        PaymentGateway mockGateway = request -> PaymentResult.approved("OK");
        CustomerVerifier verifier = customerId -> customerId.equals("CUS-1");

        PaymentGateway proxy = new CustomerVerificationProxy(verifier, mockGateway);
        facade = new RentalFacade(repository, proxy);
    }

    @Test
    void successfulRentalShouldBeConfirmed() {
        RentalRequest request = new RentalRequest(
                authorizedCustomer, vehicle,
                LocalDate.now(), LocalDate.now().plusDays(3),
                plan, services, "tok_approved"
        );

        Reservation reservation = facade.processRental(request);

        assertNotNull(reservation);
        assertTrue(reservation.isConfirmed());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(360000.0, reservation.getTotalCost()); // (100k * 3) + (20k * 3)
    }

    @Test
    void unauthorizedCustomerShouldFailViaProxy() {
        RentalRequest request = new RentalRequest(
                unauthorizedCustomer, vehicle,
                LocalDate.now(), LocalDate.now().plusDays(2),
                plan, services, "tok_approved"
        );

        Reservation reservation = facade.processRental(request);

        assertNotNull(reservation);
        assertFalse(reservation.isConfirmed());
        assertEquals(ReservationStatus.FAILED, reservation.getStatus());
        assertTrue(reservation.getReason().contains("not authorized"));
    }

    @Test
    void overlappingDatesShouldFailAvailabilityCheck() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(4);

        // First reservation confirmed
        RentalRequest firstRequest = new RentalRequest(
                authorizedCustomer, vehicle, start, end, plan, services, "tok_approved"
        );
        facade.processRental(firstRequest);

        // Second reservation overlapping
        RentalRequest overlappingRequest = new RentalRequest(
                authorizedCustomer, vehicle, start.plusDays(2), end.plusDays(2), plan, services, "tok_approved"
        );
        Reservation secondReservation = facade.processRental(overlappingRequest);

        assertFalse(secondReservation.isConfirmed());
        assertEquals(ReservationStatus.FAILED, secondReservation.getStatus());
        assertTrue(secondReservation.getReason().contains("not available"));
    }
}