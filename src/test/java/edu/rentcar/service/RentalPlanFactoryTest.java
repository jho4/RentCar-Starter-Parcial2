package edu.rentcar.service;

import edu.rentcar.factory.PlanCatalog;
import edu.rentcar.factory.PremiumPlanFactory;
import edu.rentcar.factory.RentalPlan;
import edu.rentcar.model.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalPlanFactoryTest {

    private final Vehicle vehicle = new Vehicle("V1", "Toyota", "Corolla", "Sedan", 2024, 100.0, "Available");

    @Test
    void premiumFactoryCreatesPremiumPlanWithCorrectCost() {
        RentalPlan plan = new PremiumPlanFactory().createPlan();

        assertEquals("Premium", plan.getName());
        // 100 * 3 days * 1.35 + 20 fixed fee = 425
        assertEquals(425.0, plan.calculateBaseCost(vehicle, 3), 0.001);
    }

    @Test
    void catalogFindsPlansByNameAndRejectsUnknownOnes() {
        PlanCatalog catalog = new PlanCatalog();

        assertEquals(3, catalog.getAll().size());
        assertTrue(catalog.findByName("basic").isPresent());
        assertTrue(catalog.findByName("does-not-exist").isEmpty());
    }
}