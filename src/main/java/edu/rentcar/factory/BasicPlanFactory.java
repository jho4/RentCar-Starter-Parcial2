package edu.rentcar.factory;

// Factory that creates the Basic plan.
public class BasicPlanFactory implements RentalPlanFactory {

    @Override
    public String getPlanName() {
        return "Basic";
    }

    @Override
    public RentalPlan createPlan() {
        return new BasicRentalPlan();
    }
}