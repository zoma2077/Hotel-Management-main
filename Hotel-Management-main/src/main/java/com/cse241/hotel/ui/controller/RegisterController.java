package com.cse241.hotel.ui.controller;

import com.cse241.hotel.enums.Gender;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.services.AuthService;
import com.cse241.hotel.ui.Dialogs;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextField balanceField;
    @FXML
    private TextField addressField;
    @FXML
    private ChoiceBox<Gender> genderChoice;
    @FXML
    private TextField preferencesField;
    @FXML
    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderChoice.setItems(FXCollections.observableArrayList(Gender.values()));
        genderChoice.getSelectionModel().selectFirst();
    }

    @FXML
    private void onRegister() {
        try {
            BigDecimal balance = parseBalance(balanceField.getText());
            List<String> preferences = parsePreferences(preferencesField.getText());

            Guest guest = AuthService.registerGuest(
                    usernameField.getText(),
                    passwordField.getText(),
                    dobPicker.getValue(),
                    balance,
                    addressField.getText(),
                    genderChoice.getValue(),
                    preferences
            );
            Session.setCurrentGuest(guest);
            Dialogs.info("Welcome!", "Account created for " + guest.getUsername());
            Navigator.goTo(Navigator.DASHBOARD);
        } catch (RuntimeException ex) {
            statusLabel.getStyleClass().setAll("status-label", "status-error");
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onBackToLogin() {
        Navigator.goTo(Navigator.LOGIN);
    }

    private static BigDecimal parseBalance(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Balance must be a valid number.");
        }
    }

    private static List<String> parsePreferences(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
