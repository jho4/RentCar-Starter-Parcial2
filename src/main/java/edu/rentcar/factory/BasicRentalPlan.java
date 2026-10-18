package edu.rentcar.factory;

import edu.rentcar.model.Vehicle;

// Cheapest plan: basic coverage and phone assistance only.
public class BasicRentalPlan implements RentalPlan {

    @Override
    public String getName() {
        return "Basic";
    }

    @Override
    public String getCoverage() {
        return "Basic liability insurance";
    }

    @Override
    public String getAssistance() {
        return "Phone assistance";
    }

    @Override
    public double calculateBaseCost(Vehicle vehicle, long days) {
        return vehicle.getDailyRate() * days;
    }
}