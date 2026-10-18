package edu.rentcar.builder;

import edu.rentcar.extras.NoExtraServices;
import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.factory.RentalPlan;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Vehicle;

import java.time.LocalDate;

// Builds a RentalRequest step by step and validates it before creating it.
public class RentalRequestBuilder {

    private Customer customer;
    private Vehicle vehicle;
    private LocalDate startDate;
    private LocalDate endDate;
    private RentalPlan plan;
    private ServiceComponent services;
    private String paymentToken;

    public RentalRequestBuilder customer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public RentalRequestBuilder vehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public RentalRequestBuilder dates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public RentalRequestBuilder plan(RentalPlan plan) {
        this.plan = plan;
        return this;
    }

    public RentalRequestBuilder services(ServiceComponent services) {
        this.services = services;
        return this;
    }

    public RentalRequestBuilder paymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
        return this;
    }

    // Checks every field and creates the request.
    // Throws IllegalArgumentException if something is missing or wrong.
    public RentalRequest build() {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (plan == null) {
            throw new IllegalArgumentException("Rental plan is required");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // If no extras were chosen, use the "no extras" service
        ServiceComponent finalServices =
                (services != null) ? services : new NoExtraServices();

        return new RentalRequest(customer, vehicle, startDate, endDate,
                plan, finalServices, paymentToken);
    }
}