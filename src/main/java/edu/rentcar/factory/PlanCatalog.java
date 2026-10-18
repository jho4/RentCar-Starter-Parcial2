package edu.rentcar.factory;

import java.util.List;
import java.util.Optional;

// Keeps all available plan factories in one place.
// The screen uses it to fill the plan list, and the facade uses it to find a plan by name.
public class PlanCatalog {

    private final List<RentalPlanFactory> factories = List.of(
            new BasicPlanFactory(),
            new StandardPlanFactory(),
            new PremiumPlanFactory()
    );
    public List<RentalPlanFactory> getAll() {
        return factories;
    }

    public Optional<RentalPlanFactory> findByName(String planName) {
        return factories.stream()
                .filter(factory -> factory.getPlanName().equalsIgnoreCase(planName))
                .findFirst();
    }
}