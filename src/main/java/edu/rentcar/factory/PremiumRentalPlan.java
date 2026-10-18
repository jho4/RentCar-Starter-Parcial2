package edu.rentcar.factory;

import edu.rentcar.model.Vehicle;

// Best plan: full coverage and priority assistance. 35% more plus a fixed fee.
public class PremiumRentalPlan implements RentalPlan {

    private static final double FIXED_FEE = 20.0;

    @Override
    public String getName() {
        return "Premium";
    }

    @Override
    public String getCoverage() {
        return "Full coverage with zero deductible";
    }

    @Override
    public String getAssistance() {
        return "Priority 24/7 assistance and replacement vehicle";
    }

    @Override
    public double calculateBaseCost(Vehicle vehicle, long days) {
        return vehicle.getDailyRate() * days * 1.35 + FIXED_FEE;
    }
}