package edu.rentcar.extras;

import java.util.ArrayList;
import java.util.List;

// A bundle of services. It can hold single services or other bundles,
// and it is used exactly like a single service.
public class ServiceBundle implements ServiceComponent {

    private final String name;
    private final double discountPercent;
    private final List<ServiceComponent> components = new ArrayList<>();

    public ServiceBundle(String name, double discountPercent) {
        this.name = name;
        this.discountPercent = discountPercent;
    }

    public ServiceBundle add(ServiceComponent component) {
        components.add(component);
        return this;
    }

    @Override
    public String getDescription() {
        StringBuilder text = new StringBuilder(name).append(" (");
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(components.get(i).getDescription());
        }
        return text.append(")").toString();
    }

    @Override
    public double getCost(long days) {
        double total = 0;
        for (ServiceComponent component : components) {
            total += component.getCost(days);
        }
        return total * (1 - discountPercent / 100);
    }
}