package com.cse241.hotel.ui.controller;

import com.cse241.hotel.enums.PaymentMethod;
import com.cse241.hotel.exceptions.InvalidPaymentException;
import com.cse241.hotel.model.transaction.Invoice;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.services.PaymentService;
import com.cse241.hotel.ui.Dialogs;
import com.cse241.hotel.ui.Navigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {

    @FXML
    private Label reservationLabel;
    @FXML
    private Label roomLabel;
    @FXML
    private Label datesLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private ChoiceBox<PaymentMethod> methodChoice;
    @FXML
    private Label statusLabel;

    private Reservation reservation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        methodChoice.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        methodChoice.getSelectionModel().select(PaymentMethod.CASH);
    }


    // Called by the previous screen to bind the reservation being paid for.

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        reservationLabel.setText("Reservation #" + shorten(reservation.getReservationId()));
        roomLabel.setText("Room " + reservation.getRoom().getRoomNumber()
                + " (" + reservation.getRoom().getRoomType().getName() + ")");
        datesLabel.setText(reservation.getCheckInDate() + " → " + reservation.getCheckOutDate());
        totalLabel.setText("$" + PaymentService.calculateTotal(reservation).toPlainString());
    }

    @FXML
    private void onPay() {
        if (reservation == null) {
            setError("No reservation selected.");
            return;
        }
        PaymentMethod method = methodChoice.getValue();
        if (method == null) {
            setError("Please choose a payment method.");
            return;
        }
        try {
            Invoice invoice = PaymentService.checkout(reservation, method);
            Dialogs.info(
                    "Payment successful",
                    "Invoice #" + shorten(invoice.getInvoiceId())
                            + " paid via " + invoice.getPaymentMethod()
                            + " for $" + invoice.getTotalAmount().toPlainString()
            );
            Navigator.goTo(Navigator.RESERVATIONS);
        } catch (InvalidPaymentException ex) {
            Dialogs.error("Payment failed", ex.getMessage());
            setError(ex.getMessage());
        } catch (RuntimeException ex) {
            Dialogs.error("Could not process payment", ex.getMessage());
            setError(ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Navigator.goTo(Navigator.RESERVATIONS);
    }

    private void setError(String message) {
        statusLabel.getStyleClass().setAll("status-label", "status-error");
        statusLabel.setText(message);
    }

    private static String shorten(String id) {
        return id == null || id.length() < 8 ? id : id.substring(0, 8);
    }
}
