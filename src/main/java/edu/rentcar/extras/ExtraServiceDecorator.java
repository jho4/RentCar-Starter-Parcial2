package edu.rentcar.extras;

import java.util.Objects;

// Base class for all extras. It wraps another service and adds something to it.
public abstract class ExtraServiceDecorator implements ServiceComponent {

    protected final ServiceComponent wrapped;

    protected ExtraServiceDecorator(ServiceComponent wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped service is required");
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public double getCost(long days) {
        return wrapped.getCost(days);
    }

    // Adds this extra's name to the description of the wrapped service
    protected String describeWith(String extraName) {
        if (wrapped instanceof NoExtraServices) {
            return extraName;
        }
        return wrapped.getDescription() + ", " + extraName;
    }
}