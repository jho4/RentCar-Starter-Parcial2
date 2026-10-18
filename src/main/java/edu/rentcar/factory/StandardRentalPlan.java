package edu.rentcar.factory;

import edu.rentcar.model.Vehicle;

// Middle plan: collision coverage and roadside assistance. 15% more expensive.
public class StandardRentalPlan implements RentalPlan {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public String getCoverage() {
        return "Collision damage waiver";
    }

    @Override
    public String getAssistance() {
        return "24/7 roadside assistance";
    }

    @Override
    public double calculateBaseCost(Vehicle vehicle, long days) {
        return vehicle.getDailyRate() * days * 1.15;
    }
}