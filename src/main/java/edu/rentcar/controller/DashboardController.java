package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.data.SampleData;
import edu.rentcar.model.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label customerCountLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label confirmedCountLabel;

    @FXML
    public void initialize() {
        customerCountLabel.setText(String.valueOf(SampleData.customers().size()));
        vehicleCountLabel.setText(String.valueOf(ApplicationServices.vehicleService().getVehicleCount()));
        confirmedCountLabel.setText(String.valueOf(ApplicationServices.rentalFacade().getReservations().stream()
                .filter(Reservation::isConfirmed).count()));
    }
}