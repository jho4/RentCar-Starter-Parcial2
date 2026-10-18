package edu.rentcar.service;

import edu.rentcar.builder.RentalRequestBuilder;
import edu.rentcar.factory.RentalPlan;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalRequestBuilderTest {

    private final Customer customer = new Customer("C1", "Test Customer", "test@mail.com", true);

    private final Vehicle vehicle = new Vehicle("V1", "Toyota", "Corolla", "Sedan", 2024, 100.0, "Available");

    private final RentalPlan plan = new RentalPlan() {
        public String getName() { return "Test plan"; }
        public String getCoverage() { return "Basic"; }
        public String getAssistance() { return "None"; }
        public double calculateBaseCost(Vehicle v, long days) { return 100 * days; }
    };

    @Test
    void buildsValidRequestSuccessfully() {
        RentalRequest request = new RentalRequestBuilder()
                .customer(customer)
                .vehicle(vehicle)
                .dates(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4))
                .plan(plan)
                .paymentToken("tok_approved")
                .build();

        assertEquals(3, request.getRentalDays());
        assertEquals(0, request.getServices().getCost(3));
    }

    @Test
    void failsWhenEndDateIsBeforeStartDate() {
        RentalRequestBuilder builder = new RentalRequestBuilder()
                .customer(customer)
                .vehicle(vehicle)
                .dates(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 5))
                .plan(plan)
                .paymentToken("tok_approved");

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}