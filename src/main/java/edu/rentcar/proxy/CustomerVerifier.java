package edu.rentcar.proxy;

/**
 * Checks if a customer is authorized to continue with a payment.
 * Implementations may throw a RuntimeException if the verification service fails;
 * the Proxy (RF-006) must handle that case in a controlled way.
 */
public interface CustomerVerifier {

    boolean isAuthorized(String customerId);
}