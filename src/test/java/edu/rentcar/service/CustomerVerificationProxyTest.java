package edu.rentcar.service;

import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;
import edu.rentcar.payment.PaymentStatus;
import edu.rentcar.proxy.CustomerVerificationProxy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerVerificationProxyTest {

    private final PaymentRequest request = new PaymentRequest("RES-1", "CUS-1", 100.0, "tok_approved");

    @Test
    void authorizedCustomerReachesThePaymentGateway() {
        AtomicInteger calls = new AtomicInteger();
        PaymentGateway real = r -> {
            calls.incrementAndGet();
            return PaymentResult.approved("OK");
        };

        PaymentResult result = new CustomerVerificationProxy(id -> true, real).pay(request);

        assertEquals(PaymentStatus.APPROVED, result.status());
        assertEquals(1, calls.get());
    }

    @Test
    void unauthorizedCustomerNeverCallsThePaymentGateway() {
        AtomicInteger calls = new AtomicInteger();
        PaymentGateway real = r -> {
            calls.incrementAndGet();
            return PaymentResult.approved("OK");
        };

        PaymentResult result = new CustomerVerificationProxy(id -> false, real).pay(request);

        assertEquals(PaymentStatus.DECLINED, result.status());
        assertEquals(0, calls.get());
    }

    @Test
    void verificationErrorIsHandledAndDoesNotCallTheGateway() {
        AtomicInteger calls = new AtomicInteger();
        PaymentGateway real = r -> {
            calls.incrementAndGet();
            return PaymentResult.approved("OK");
        };

        PaymentResult result = new CustomerVerificationProxy(id -> {
            throw new IllegalStateException("verification service down");
        }, real).pay(request);

        assertEquals(PaymentStatus.TECHNICAL_ERROR, result.status());
        assertEquals(0, calls.get());
    }
}