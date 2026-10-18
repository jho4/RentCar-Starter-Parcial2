package edu.rentcar.controller;

import edu.rentcar.data.SampleData;
import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.factory.RentalPlan;
import edu.rentcar.factory.RentalPlanFactory;
import edu.rentcar.model.*;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.proxy.CustomerVerificationProxy;
import edu.rentcar.proxy.CustomerVerifier;
import edu.rentcar.repository.ReservationRepository;
import edu.rentcar.service.RentalFacade;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Controller for the reservation workflow screen, connecting the JavaFX UI
 * with the RentalFacade, Payment Adapter, and Customer Verification Proxy.
 */
public class ReservationController {

    private final ReservationRepository reservationRepository = new ReservationRepository();
    // Simple inline implementation of CustomerVerifier using SampleData
    private final CustomerVerifier customerVerifier = customerId ->
            SampleData.customers().stream()
                    .filter(c -> c.getId().equals(customerId))
                    .findFirst()
                    .map(Customer::isAuthorized)
                    .orElse(false);

    private final PaymentGateway paymentGateway = new CustomerVerificationProxy(
            customerVerifier,
            new PaymentApiAdapter("http://localhost:8080")
    );

    private final RentalFacade rentalFacade = new RentalFacade(reservationRepository, paymentGateway);

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField paymentTokenField;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        customerComboBox.getItems().setAll(SampleData.customers());
        vehicleComboBox.getItems().setAll(SampleData.vehicles());
        paymentTokenField.setText("tok_approved");
    }

    @FXML
    private void processReservation() {
        Customer customer = customerComboBox.getValue();
        Vehicle vehicle = vehicleComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String paymentToken = paymentTokenField.getText();

        if (customer == null || vehicle == null || startDate == null || endDate == null || paymentToken.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all rental fields.");
            return;
        }

        // Default basic rental plan and empty services for standard view flow
        RentalPlan plan = new RentalPlan() {
            @Override public String getName() { return "Standard"; }
            @Override public String getCoverage() { return "Basic Coverage"; }
            @Override public String getAssistance() { return "Roadside Assistance"; }
            @Override public double calculateBaseCost(Vehicle v, long days) { return v.getDailyRate() * days; }
        };

        ServiceComponent noServices = new ServiceComponent() {
            @Override public String getDescription() { return "No extra services"; }
            @Override public double getCost(long days) { return 0.0; }
        };

        RentalRequest request = new RentalRequest(
                customer, vehicle, startDate, endDate, plan, noServices, paymentToken
        );

        Reservation reservation = rentalFacade.processRental(request);

        if (reservation.isConfirmed()) {
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                    "ID: " + reservation.getId() + "\nTotal Cost: $" + reservation.getTotalCost() + "\n" + reservation.getReason());
        } else {
            showAlert(Alert.AlertType.ERROR, "Reservation Failed",
                    "Status: " + reservation.getStatus() + "\nReason: " + reservation.getReason());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}