package edu.rentcar.payment;

/**
 * Port used by the business logic to charge a payment.
 * The Adapter (RF-004) and the Proxy (RF-006) implement this interface, so the rest of the system
 * does not depend on the format of the external payment API.
 * Implementations must not throw for payment problems: they return a {@link PaymentResult}.
 */
public interface PaymentGateway {

    PaymentResult pay(PaymentRequest request);
}