package edu.rentcar.controller;

import edu.rentcar.data.SampleData;
import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.factory.RentalPlan;
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
    @FXML private ListView<Reservation> reservationListView;

    @FXML
    public void initialize() {
        customerComboBox.getItems().setAll(SampleData.customers());
        vehicleComboBox.getItems().setAll(SampleData.vehicles());

        // Custom display formatting for Customer ComboBox
        customerComboBox.setCellFactory(param -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty || customer == null ? null : customer.getFullName() + " (" + customer.getId() + ")");
            }
        });
        customerComboBox.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty || customer == null ? null : customer.getFullName() + " (" + customer.getId() + ")");
            }
        });

        // Custom display formatting for Vehicle ComboBox
        vehicleComboBox.setCellFactory(param -> new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                setText(empty || vehicle == null ? null : vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getCategory() + " - $" + vehicle.getDailyRate() + ")");
            }
        });
        vehicleComboBox.setButtonCell(new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                setText(empty || vehicle == null ? null : vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getCategory() + " - $" + vehicle.getDailyRate() + ")");
            }
        });

        // Custom display formatting for Reservation History ListView
        if (reservationListView != null) {
            reservationListView.setCellFactory(param -> new ListCell<Reservation>() {
                @Override
                protected void updateItem(Reservation res, boolean empty) {
                    super.updateItem(res, empty);
                    if (empty || res == null) {
                        setText(null);
                    } else {
                        setText(String.format("ID: %s | Status: %s | Total: $%.2f | Details: %s",
                                res.getId(), res.getStatus(), res.getTotalCost(), res.getReason()));
                    }
                }
            });
            loadReservationHistory();
        }
    }

    @FXML
    private void processReservation() {
        Customer customer = customerComboBox.getValue();
        Vehicle vehicle = vehicleComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Automated default payment token for external gateway
        String paymentToken = "tok_approved";

        if (customer == null || vehicle == null || startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all rental fields.");
            return;
        }

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

        loadReservationHistory();

        if (reservation.isConfirmed()) {
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                    "ID: " + reservation.getId() +
                            "\nCustomer: " + customer.getFullName() +
                            "\nVehicle: " + vehicle.getBrand() + " " + vehicle.getModel() +
                            "\nTotal Cost: $" + reservation.getTotalCost() +
                            "\nPayment Token: " + paymentToken +
                            "\nStatus: " + reservation.getReason());
        } else {
            showAlert(Alert.AlertType.ERROR, "Reservation Failed",
                    "Status: " + reservation.getStatus() + "\nReason: " + reservation.getReason());
        }
    }

    @FXML
    private void refreshHistory() {
        loadReservationHistory();
    }

    private void loadReservationHistory() {
        if (reservationListView != null) {
            reservationListView.getItems().setAll(reservationRepository.findAll());
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