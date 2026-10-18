package edu.rentcar.proxy;

import edu.rentcar.payment.*;

/**
 * Proxy pattern (RF-006): Verifies if the customer is authorized using the CustomerVerifier
 * before invoking the payment gateway If unauthorized, it blocks the transaction immediately.
 */
public class CustomerVerificationProxy implements PaymentGateway {
    private final CustomerVerifier verifier;
    private final PaymentGateway realPaymentGateway;

    public CustomerVerificationProxy(CustomerVerifier verifier, PaymentGateway realPaymentGateway) {
        this.verifier = verifier;
        this.realPaymentGateway = realPaymentGateway;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        try {
            boolean authorized = verifier.isAuthorized(request.customerId());
            if (!authorized) {
                return PaymentResult.declined("Customer is not authorized to perform rentals.");
            }
        } catch (Exception e) {
            return PaymentResult.technicalError("Customer verification service error: " + e.getMessage());
        }

        // If authorized, proceed with the actual payment gateway (Adapter)
        return realPaymentGateway.pay(request);
    }
}