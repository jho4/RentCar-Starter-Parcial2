package edu.rentcar.payment;

/** Outcome of a payment, independent of the format of any external provider. */
public enum PaymentStatus {
    APPROVED,
    DECLINED,
    TECHNICAL_ERROR
}