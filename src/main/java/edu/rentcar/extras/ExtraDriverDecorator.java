package edu.rentcar.extras;

// Extra service: additional driver. Fixed cost of 30, no matter how many days.
public class ExtraDriverDecorator extends ExtraServiceDecorator {

    private static final double FIXED_COST = 30.0;

    public ExtraDriverDecorator(ServiceComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getDescription() {
        return describeWith("Extra driver");
    }

    @Override
    public double getCost(long days) {
        return wrapped.getCost(days) + FIXED_COST;
    }
}