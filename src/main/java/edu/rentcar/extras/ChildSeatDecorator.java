package edu.rentcar.extras;

// Extra service: child seat. Costs 7 per day.
public class ChildSeatDecorator extends ExtraServiceDecorator {

    private static final double DAILY_COST = 7.0;

    public ChildSeatDecorator(ServiceComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getDescription() {
        return describeWith("Child seat");
    }

    @Override
    public double getCost(long days) {
        return wrapped.getCost(days) + DAILY_COST * days;
    }
}