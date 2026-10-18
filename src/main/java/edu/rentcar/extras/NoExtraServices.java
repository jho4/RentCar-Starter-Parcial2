package edu.rentcar.extras;

// Represents a rental with no extra services. It always costs zero.
public class NoExtraServices implements ServiceComponent {

    @Override
    public String getDescription() {
        return "No extra services";
    }

    @Override
    public double getCost(long days) {
        return 0;
    }
}