package edu.rentcar.payment;

/** Payment data in the internal format of the application. */
public record PaymentRequest(String reservationId, String customerId, double amount, String paymentToken) {
}