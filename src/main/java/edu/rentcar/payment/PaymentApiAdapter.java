package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;

/**
 * Adapter pattern (RF-004): Adapts the external payment client provided by the instructor[cite: 42]
 * to the internal PaymentGateway interface of the application[cite: 37].
 */
public class PaymentApiAdapter implements PaymentGateway {
    private final PaymentApiClient apiClient;

    public PaymentApiAdapter(String baseUrl) {
        this.apiClient = new PaymentApiClient(baseUrl);
    }

    public PaymentApiAdapter(PaymentApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        PaymentApiRequest apiRequest = new PaymentApiRequest(
                request.reservationId(),
                request.customerId(),
                request.amount(),
                request.paymentToken()
        );

        try {
            PaymentApiResponse response = apiClient.pay(apiRequest);
            if (response.isApproved()) {
                return PaymentResult.approved(response.body());
            } else if (response.isDeclined()) {
                return PaymentResult.declined(response.body());
            } else {
                return PaymentResult.technicalError("Payment API returned status: " + response.httpStatus());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.technicalError("Payment API communication error: " + e.getMessage());
        }
    }
}