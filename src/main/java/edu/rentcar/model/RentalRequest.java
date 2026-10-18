package edu.rentcar.model;

import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.factory.RentalPlan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Immutable and valid rental request. It is created by the RentalRequestBuilder,
 * which is responsible for validating the data before calling this constructor.
 */
public class RentalRequest {
    private final Customer customer;
    private final Vehicle vehicle;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final RentalPlan plan;
    private final ServiceComponent services;
    private final String paymentToken;

    public RentalRequest(Customer customer, Vehicle vehicle, LocalDate startDate, LocalDate endDate,
                         RentalPlan plan, ServiceComponent services, String paymentToken) {
        this.customer = Objects.requireNonNull(customer, "customer");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        this.plan = Objects.requireNonNull(plan, "plan");
        this.services = Objects.requireNonNull(services, "services");
        this.paymentToken = Objects.requireNonNull(paymentToken, "paymentToken");
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public RentalPlan getPlan() {
        return plan;
    }

    /** Extra services and bundles selected for the rental (may represent "no extras"). */
    public ServiceComponent getServices() {
        return services;
    }

    /** Token sent to the payment API. It comes from the payment method selected by the user. */
    public String getPaymentToken() {
        return paymentToken;
    }

    /** Number of rental days: end date minus start date. */
    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}