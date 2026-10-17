package edu.rentcar.factory;

import edu.rentcar.model.Vehicle;

/**
 * A rental plan (modality): a coherent combination of rate policy, coverage and assistance.
 * Instances are created by a {@link RentalPlanFactory}.
 */
public interface RentalPlan {

    /** Name of the plan, for example "Economy". */
    String getName();

    /** Short description of the coverage included in the plan. */
    String getCoverage();

    /** Short description of the assistance included in the plan. */
    String getAssistance();

    /** Base cost of renting the vehicle for the given number of days, before extra services. */
    double calculateBaseCost(Vehicle vehicle, long days);
}