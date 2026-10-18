package edu.rentcar.payment;

/**
 * Payment methods that the customer can choose in the interface.
 * The token sent to the payment API belongs to the method and is never shown to the user:
 * in a real system it would be produced by the card tokenization service.
 */
public enum PaymentMethod {
    VISA_4242("Visa ending in 4242", "tok_approved"),
    MASTERCARD_0002("Mastercard ending in 0002", "tok_declined"),
    DEBIT_9999("Debit card ending in 9999", "tok_api_error");

    private final String label;
    private final String token;

    PaymentMethod(String label, String token) {
        this.label = label;
        this.token = token;
    }

    public String getLabel() {
        return label;
    }

    public String getToken() {
        return token;
    }
}