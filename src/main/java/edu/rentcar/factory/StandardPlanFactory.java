package edu.rentcar.factory;

// Factory that creates the Standard plan.
public class StandardPlanFactory implements RentalPlanFactory {

    @Override
    public String getPlanName() {
        return "Standard";
    }

    @Override
    public RentalPlan createPlan() {
        return new StandardRentalPlan();
    }
}