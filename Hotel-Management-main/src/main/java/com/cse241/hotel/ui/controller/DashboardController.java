package com.cse241.hotel.ui.controller;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label upcomingLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Guest guest = Session.getCurrentGuest();
        if (guest == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }
        welcomeLabel.setText("Welcome, " + guest.getUsername() + "!");
        balanceLabel.setText("Account balance: $" + format(guest.getBalance()));

        long upcoming = HotelDatabase.RESERVATIONS.stream()
                .filter(r -> r.getGuest() == guest)
                .filter(r -> r.getStatus() == ReservationStatus.PENDING
                        || r.getStatus() == ReservationStatus.CONFIRMED)
                .count();
        upcomingLabel.setText(upcoming + " active reservation(s).");
    }

    @FXML
    private void onBrowseRooms() {
        Navigator.goTo(Navigator.ROOMS);
    }

    @FXML
    private void onMyReservations() {
        Navigator.goTo(Navigator.RESERVATIONS);
    }

    @FXML
    private void onChat() {
        Navigator.goTo(Navigator.CHAT);
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Navigator.goTo(Navigator.LOGIN);
    }

    private static String format(BigDecimal value) {
        return value == null ? "0.00" : value.toPlainString();
    }
}
