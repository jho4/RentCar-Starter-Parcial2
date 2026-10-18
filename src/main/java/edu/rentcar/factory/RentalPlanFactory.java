package edu.rentcar.factory;

/**
 * Abstract Factory for rental plans. Each concrete factory builds the complete family of objects
 * of one modality (rate policy, coverage and assistance).
 * The owner of this package may add more creation methods later; the rest of the system only
 * needs {@link #getPlanName()} and {@link #createPlan()}.
 */
public interface RentalPlanFactory {

    /** Name of the plan this factory creates, to show it in the interface. */
    String getPlanName();

    /** Creates the plan with its coherent policies. */
    RentalPlan createPlan();
}