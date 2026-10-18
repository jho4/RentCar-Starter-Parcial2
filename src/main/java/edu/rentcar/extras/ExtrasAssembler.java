package edu.rentcar.extras;

import java.util.Set;

/**
 * Builds the {@link ServiceComponent} for a set of selected extras.
 * Individual extras are added with Decorators (RF-005) and the bundle is a Composite (RF-007),
 * so no class is needed for each combination of services.
 */
public final class ExtrasAssembler {

    private ExtrasAssembler() {
    }

    public static ServiceComponent assemble(Set<ExtraOption> selected) {
        ServiceComponent service = selected.contains(ExtraOption.ROAD_TRIP_BUNDLE)
                ? createRoadTripBundle()
                : new NoExtraServices();

        if (selected.contains(ExtraOption.GPS)) {
            service = new GpsDecorator(service);
        }
        if (selected.contains(ExtraOption.CHILD_SEAT)) {
            service = new ChildSeatDecorator(service);
        }
        if (selected.contains(ExtraOption.EXTRA_DRIVER)) {
            service = new ExtraDriverDecorator(service);
        }
        return service;
    }

    /** A bundle that contains single services and another (nested) bundle. */
    static ServiceBundle createRoadTripBundle() {
        ServiceBundle familyPack = new ServiceBundle("Family Pack", 5)
                .add(new SingleService("Booster cushion", 3))
                .add(new SingleService("Cooler box", 2));
        return new ServiceBundle("Road Trip Bundle", 10)
                .add(new SingleService("Roadside kit", 4))
                .add(new SingleService("Toll pass", 6))
                .add(familyPack);
    }
}