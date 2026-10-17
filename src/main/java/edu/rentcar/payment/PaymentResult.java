package edu.rentcar.payment;

/** Result of a payment in the internal format of the application. */
public record PaymentResult(PaymentStatus status, String message) {

    public static PaymentResult approved(String message) {
        return new PaymentResult(PaymentStatus.APPROVED, message);
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(PaymentStatus.DECLINED, message);
    }

    public static PaymentResult technicalError(String message) {
        return new PaymentResult(PaymentStatus.TECHNICAL_ERROR, message);
    }

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }
}