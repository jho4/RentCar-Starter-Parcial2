package edu.rentcar.service;

import com.sun.net.httpserver.HttpServer;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;
import edu.rentcar.payment.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the Adapter (RF-004) against a local fake payment API, so the real HTTP client is exercised
 * without depending on the external payment-api.jar being started.
 */
class PaymentApiAdapterTest {

    private final AtomicReference<String> receivedBody = new AtomicReference<>();
    private HttpServer server;

    private PaymentApiAdapter startFakeApi(int httpStatus, String responseBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/payments", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(httpStatus, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        return new PaymentApiAdapter("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopFakeApi() {
        if (server != null) {
            server.stop(0);
        }
    }

    private PaymentRequest request() {
        return new PaymentRequest("RES-1001", "CUS-1001", 350000.0, "tok_approved");
    }

    @Test
    void approvedResponseIsMappedToApprovedResult() throws IOException {
        PaymentResult result = startFakeApi(200, "APPROVED").pay(request());

        assertEquals(PaymentStatus.APPROVED, result.status());
        assertTrue(result.isApproved());
    }

    @Test
    void notApprovedResponseIsMappedToDeclinedResult() throws IOException {
        PaymentResult result = startFakeApi(200, "NOT_APPROVED").pay(request());

        assertEquals(PaymentStatus.DECLINED, result.status());
        assertEquals(false, result.isApproved());
    }

    @Test
    void httpServiceUnavailableIsMappedToTechnicalError() throws IOException {
        PaymentResult result = startFakeApi(503, "SERVICE_UNAVAILABLE").pay(request());

        assertEquals(PaymentStatus.TECHNICAL_ERROR, result.status());
    }

    @Test
    void connectionProblemIsMappedToTechnicalErrorWithoutThrowing() {
        // Nothing listens on this port, so the connection is refused.
        PaymentApiAdapter adapter = new PaymentApiAdapter("http://localhost:1");

        PaymentResult result = adapter.pay(request());

        assertEquals(PaymentStatus.TECHNICAL_ERROR, result.status());
    }

    @Test
    void internalRequestIsTranslatedToTheApiFormat() throws IOException {
        startFakeApi(200, "APPROVED").pay(request());

        String body = receivedBody.get();
        assertTrue(body.contains("RES-1001"));
        assertTrue(body.contains("CUS-1001"));
        assertTrue(body.contains("350000"));
        assertTrue(body.contains("tok_approved"));
    }
}