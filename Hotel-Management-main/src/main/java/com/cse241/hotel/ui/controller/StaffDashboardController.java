package com.cse241.hotel.ui.controller;

import com.cse241.hotel.enums.Role;
import com.cse241.hotel.model.user.Staff;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Button manageRoomsButton;
    @FXML
    private Button manageReservationsButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }
        welcomeLabel.setText("Welcome, " + staff.getUsername() + "!");
        roleLabel.setText("Role: " + staff.getRole().name());

        boolean isAdmin = staff.getRole() == Role.ADMIN;
        manageRoomsButton.setDisable(!isAdmin);
        manageRoomsButton.setVisible(isAdmin);
        manageRoomsButton.setManaged(isAdmin);

        boolean isReceptionist = staff.getRole() == Role.RECEPTIONIST;
        manageReservationsButton.setDisable(!isReceptionist);
        manageReservationsButton.setVisible(isReceptionist);
        manageReservationsButton.setManaged(isReceptionist);
    }

    @FXML
    private void onManageRooms() {
        Navigator.goTo(Navigator.ADMIN_ROOMS);
    }

    @FXML
    private void onManageReservations() {
        Navigator.goTo(Navigator.RECEPTIONIST_RESERVATIONS);
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
}

