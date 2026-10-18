package edu.rentcar.extras;

// A single service with a name and a cost per day. It is the basic piece of a bundle.
public class SingleService implements ServiceComponent {

    private final String name;
    private final double dailyCost;

    public SingleService(String name, double dailyCost) {
        this.name = name;
        this.dailyCost = dailyCost;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public double getCost(long days) {
        return dailyCost * days;
    }
}