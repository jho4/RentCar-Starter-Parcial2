package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.builder.RentalRequestBuilder;
import edu.rentcar.data.SampleData;
import edu.rentcar.extras.ExtraOption;
import edu.rentcar.extras.ExtrasAssembler;
import edu.rentcar.factory.RentalPlanFactory;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.model.Vehicle;
import edu.rentcar.service.RentalFacade;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Controller of the reservation screen. It only reads the controls and shows results:
 * the request is built by {@link RentalRequestBuilder} and the workflow runs in {@link RentalFacade}.
 */
public class ReservationController {

    private static final String ALL_STATUSES = "ALL";

    private final RentalFacade rentalFacade = ApplicationServices.rentalFacade();

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<RentalPlanFactory> planComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox gpsCheckBox;
    @FXML private CheckBox childSeatCheckBox;
    @FXML private CheckBox extraDriverCheckBox;
    @FXML private CheckBox bundleCheckBox;
    @FXML private ComboBox<String> paymentTokenComboBox;
    @FXML private Label planDetailsLabel;
    @FXML private Label availabilityLabel;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> customerColumn;
    @FXML private TableColumn<Reservation, String> vehicleColumn;
    @FXML private TableColumn<Reservation, String> datesColumn;
    @FXML private TableColumn<Reservation, String> planColumn;
    @FXML private TableColumn<Reservation, String> totalColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, String> detailsColumn;

    @FXML
    public void initialize() {
        setUpSelectors();
        setUpDatePickers();
        setUpTable();

        statusFilterComboBox.getItems().add(ALL_STATUSES);
        for (ReservationStatus status : ReservationStatus.values()) {
            statusFilterComboBox.getItems().add(status.name());
        }
        statusFilterComboBox.setValue(ALL_STATUSES);

        paymentTokenComboBox.getItems().setAll("tok_approved", "tok_declined", "tok_api_error");
        paymentTokenComboBox.setValue("tok_approved");

        planComboBox.getItems().setAll(ApplicationServices.planCatalog().getAll());
        planComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(RentalPlanFactory factory) {
                return factory == null ? "" : factory.getPlanName();
            }
            @Override public RentalPlanFactory fromString(String text) {
                return null;
            }
        });
        planComboBox.getSelectionModel().selectFirst();

        refreshPreview();
        loadReservationHistory();
    }

    @FXML
    private void refreshPreview() {
        RentalPlanFactory factory = planComboBox.getValue();
        if (factory == null) {
            planDetailsLabel.setText("");
        } else {
            var plan = factory.createPlan();
            planDetailsLabel.setText("Coverage: " + plan.getCoverage() + "\nAssistance: " + plan.getAssistance());
        }

        Vehicle vehicle = vehicleComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        availabilityLabel.setText("");
        if (vehicle != null && start != null && end != null && end.isAfter(start)) {
            boolean available = rentalFacade.isVehicleAvailable(vehicle, start, end);
            availabilityLabel.setText(available
                    ? "Vehicle available for these dates."
                    : "Vehicle NOT available for these dates.");
            availabilityLabel.setStyle("-fx-text-fill: " + (available ? "#15803d" : "#b91c1c") + ";");
        }

        try {
            RentalRequest request = buildRequest();
            long days = request.getRentalDays();
            double base = request.getPlan().calculateBaseCost(request.getVehicle(), days);
            double extras = request.getServices().getCost(days);
            summaryLabel.setText(String.format(
                    "%d day(s) | Plan: %s | Base: $%.2f%nExtras: %s ($%.2f)%nTOTAL: $%.2f",
                    days, request.getPlan().getName(), base,
                    request.getServices().getDescription(), extras, base + extras));
        } catch (IllegalArgumentException e) {
            summaryLabel.setText("Complete the form to see the total cost.\n(" + e.getMessage() + ")");
        }
    }

    @FXML
    private void processReservation() {
        messageLabel.setText("");
        RentalRequest request;
        try {
            request = buildRequest();
        } catch (IllegalArgumentException e) {
            showMessage("Invalid request: " + e.getMessage(), true);
            showAlert(Alert.AlertType.WARNING, "Invalid request", e.getMessage());
            return;
        }

        Reservation reservation = rentalFacade.processRental(request);
        loadReservationHistory();
        refreshPreview();

        switch (reservation.getStatus()) {
            case CONFIRMED -> {
                showMessage("Reservation " + reservation.getId() + " confirmed.", false);
                showAlert(Alert.AlertType.INFORMATION, "Reservation confirmed",
                        "ID: " + reservation.getId()
                                + "\nCustomer: " + request.getCustomer().getFullName()
                                + "\nVehicle: " + request.getVehicle().getDisplayName()
                                + "\nPlan: " + request.getPlan().getName()
                                + "\nExtras: " + request.getServices().getDescription()
                                + String.format("%nTotal cost: $%.2f", reservation.getTotalCost()));
            }
            case PENDING -> {
                showMessage("Reservation " + reservation.getId() + " is pending: " + reservation.getReason(), true);
                showAlert(Alert.AlertType.WARNING, "Reservation pending",
                        "The payment could not be completed (technical problem).\n" + reservation.getReason());
            }
            case FAILED -> {
                showMessage("Reservation " + reservation.getId() + " failed: " + reservation.getReason(), true);
                showAlert(Alert.AlertType.ERROR, "Reservation failed", reservation.getReason());
            }
        }
    }

    @FXML
    private void clearForm() {
        customerComboBox.setValue(null);
        vehicleComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        gpsCheckBox.setSelected(false);
        childSeatCheckBox.setSelected(false);
        extraDriverCheckBox.setSelected(false);
        bundleCheckBox.setSelected(false);
        paymentTokenComboBox.setValue("tok_approved");
        planComboBox.getSelectionModel().selectFirst();
        messageLabel.setText("");
        refreshPreview();
    }

    @FXML
    private void refreshHistory() {
        loadReservationHistory();
    }

    private RentalRequest buildRequest() {
        RentalPlanFactory factory = planComboBox.getValue();
        return new RentalRequestBuilder()
                .customer(customerComboBox.getValue())
                .vehicle(vehicleComboBox.getValue())
                .dates(startDatePicker.getValue(), endDatePicker.getValue())
                .plan(factory == null ? null : factory.createPlan())
                .services(ExtrasAssembler.assemble(selectedExtras()))
                .paymentToken(paymentTokenComboBox.getValue())
                .build();
    }

    private Set<ExtraOption> selectedExtras() {
        Set<ExtraOption> selected = EnumSet.noneOf(ExtraOption.class);
        if (gpsCheckBox.isSelected()) selected.add(ExtraOption.GPS);
        if (childSeatCheckBox.isSelected()) selected.add(ExtraOption.CHILD_SEAT);
        if (extraDriverCheckBox.isSelected()) selected.add(ExtraOption.EXTRA_DRIVER);
        if (bundleCheckBox.isSelected()) selected.add(ExtraOption.ROAD_TRIP_BUNDLE);
        return selected;
    }

    private void setUpSelectors() {
        customerComboBox.getItems().setAll(SampleData.customers());
        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) {
                return c == null ? "" : c.getFullName() + " (" + c.getId() + ")";
            }
            @Override public Customer fromString(String text) {
                return null;
            }
        });

        vehicleComboBox.getItems().setAll(ApplicationServices.vehicleService().getAllVehicles());
        vehicleComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) {
                return v == null ? "" : v.getDisplayName() + " (" + v.getCategory() + " - $" + v.getDailyRate() + "/day)";
            }
            @Override public Vehicle fromString(String text) {
                return null;
            }
        });
        // Vehicles may be registered in the Vehicles screen after this one was opened.
        vehicleComboBox.setOnShowing(event -> {
            Vehicle selected = vehicleComboBox.getValue();
            vehicleComboBox.getItems().setAll(ApplicationServices.vehicleService().getAllVehicles());
            vehicleComboBox.getItems().stream()
                    .filter(v -> selected != null && v.getId().equals(selected.getId()))
                    .findFirst().ifPresent(vehicleComboBox::setValue);
        });
    }

    private void setUpDatePickers() {
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate start = startDatePicker.getValue();
                LocalDate min = start == null ? LocalDate.now().plusDays(1) : start.plusDays(1);
                setDisable(empty || date.isBefore(min));
            }
        });
    }

    private void setUpTable() {
        bindColumn(idColumn, Reservation::getId);
        bindColumn(customerColumn, r -> r.getRequest().getCustomer().getFullName());
        bindColumn(vehicleColumn, r -> r.getRequest().getVehicle().getDisplayName());
        bindColumn(datesColumn, r -> r.getStartDate() + " -> " + r.getEndDate());
        bindColumn(planColumn, r -> r.getRequest().getPlan().getName()
                + " + " + r.getRequest().getServices().getDescription());
        bindColumn(totalColumn, r -> String.format("$%.2f", r.getTotalCost()));
        bindColumn(statusColumn, r -> r.getStatus().name());
        bindColumn(detailsColumn, Reservation::getReason);

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty ? null : status);
                if (empty || status == null) {
                    setStyle("");
                    return;
                }
                String color = switch (ReservationStatus.valueOf(status)) {
                    case CONFIRMED -> "#15803d";
                    case PENDING -> "#b45309";
                    case FAILED -> "#b91c1c";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });
        reservationTable.setPlaceholder(new Label("No reservations registered yet."));
    }

    private void bindColumn(TableColumn<Reservation, String> column, Function<Reservation, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
    }

    @FXML
    private void loadReservationHistory() {
        String filter = statusFilterComboBox.getValue();
        reservationTable.getItems().setAll(rentalFacade.getReservations().stream()
                .filter(r -> filter == null || ALL_STATUSES.equals(filter) || r.getStatus().name().equals(filter))
                .toList());
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + (error ? "#b91c1c" : "#15803d") + "; -fx-font-weight: bold;");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}