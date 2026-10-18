package edu.rentcar.factory;

// Factory that creates the Premium plan.
public class PremiumPlanFactory implements RentalPlanFactory {

    @Override
    public String getPlanName() {
        return "Premium";
    }

    @Override
    public RentalPlan createPlan() {
        return new PremiumRentalPlan();
    }
}