package com.cse241.hotel.ui.controller;

import com.cse241.hotel.enums.Role;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.model.user.Staff;
import com.cse241.hotel.services.AuthService;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private static final String ROLE_GUEST = "Guest";
    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_RECEPTIONIST = "Receptionist";

    @FXML
    private ChoiceBox<String> roleChoice;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private javafx.scene.control.Button registerButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleChoice.getItems().setAll(ROLE_GUEST, ROLE_ADMIN, ROLE_RECEPTIONIST);
        roleChoice.setValue(ROLE_GUEST);
        roleChoice.valueProperty().addListener((obs, oldV, newV) -> {
            boolean isGuest = ROLE_GUEST.equals(newV);
            if (registerButton != null) {
                registerButton.setDisable(!isGuest);
                registerButton.setVisible(isGuest);
                registerButton.setManaged(isGuest);
            }
        });
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            String role = roleChoice == null ? ROLE_GUEST : roleChoice.getValue();
            if (ROLE_ADMIN.equals(role)) {
                Staff staff = AuthService.loginStaff(Role.ADMIN, username, password);
                Session.setCurrentStaff(staff);
                Navigator.goTo(Navigator.STAFF_DASHBOARD);
            } else if (ROLE_RECEPTIONIST.equals(role)) {
                Staff staff = AuthService.loginStaff(Role.RECEPTIONIST, username, password);
                Session.setCurrentStaff(staff);
                Navigator.goTo(Navigator.STAFF_DASHBOARD);
            } else {
                Guest guest = AuthService.loginGuest(username, password);
                Session.setCurrentGuest(guest);
                Navigator.goTo(Navigator.DASHBOARD);
            }
            statusLabel.setText("");
        } catch (RuntimeException ex) {
            statusLabel.getStyleClass().setAll("status-label", "status-error");
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onGoToRegister() {
        Navigator.goTo(Navigator.REGISTER);
    }
}
