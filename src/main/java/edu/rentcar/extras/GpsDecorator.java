package edu.rentcar.extras;

// Extra service: GPS. Costs 5 per day.
public class GpsDecorator extends ExtraServiceDecorator {

    private static final double DAILY_COST = 5.0;

    public GpsDecorator(ServiceComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getDescription() {
        return describeWith("GPS");
    }

    @Override
    public double getCost(long days) {
        return wrapped.getCost(days) + DAILY_COST * days;
    }
}