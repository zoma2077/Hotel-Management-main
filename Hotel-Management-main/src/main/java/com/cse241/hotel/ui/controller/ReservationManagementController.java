package com.cse241.hotel.ui.controller;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.services.ReservationService;
import com.cse241.hotel.ui.Dialogs;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import com.cse241.hotel.ui.concurrent.FxExecutors;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ReservationManagementController implements Initializable {

    @FXML
    private TableView<Reservation> table;
    @FXML
    private TableColumn<Reservation, String> idCol;
    @FXML
    private TableColumn<Reservation, String> roomCol;
    @FXML
    private TableColumn<Reservation, String> checkInCol;
    @FXML
    private TableColumn<Reservation, String> checkOutCol;
    @FXML
    private TableColumn<Reservation, String> statusCol;
    @FXML
    private Label statusLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button payButton;

    private final ExecutorService loadExecutor = FxExecutors.newSingleDaemonExecutor("reservation-load");
    private final ReservationLoadService loadService = new ReservationLoadService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.getCurrentGuest() == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }
        configureTable();
        loadService.setExecutor(loadExecutor);
        loadService.setOnSucceeded(evt -> applyReservationData(loadService.getValue()));
        loadService.setOnFailed(evt -> {
            Throwable ex = loadService.getException();
            setStatus(ex == null ? "Failed to load reservations." : ex.getMessage(), "status-error");
        });
        refreshAsync();

        confirmButton.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull()
        );
        cancelButton.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull()
        );
        payButton.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    private void configureTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(shorten(c.getValue().getReservationId())));
        roomCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom().getRoomNumber()
                + " (" + c.getValue().getRoom().getRoomType().getName() + ")"));
        checkInCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        checkOutCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
    }

    @FXML
    private void onRefresh() {
        refreshAsync();
    }

    @FXML
    private void onBack() {
        loadService.cancel();
        loadExecutor.shutdownNow();
        Navigator.goTo(Navigator.DASHBOARD);
    }

    @FXML
    private void onCancel() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() == ReservationStatus.CANCELLED
                || selected.getStatus() == ReservationStatus.COMPLETED) {
            Dialogs.warning("Cannot cancel",
                    "Reservation is already " + selected.getStatus() + ".");
            return;
        }
        if (!Dialogs.confirm("Cancel reservation?",
                "Reservation for room " + selected.getRoom().getRoomNumber()
                        + " (" + selected.getCheckInDate() + " - " + selected.getCheckOutDate() + ")")) {
            return;
        }
        boolean ok = ReservationService.cancelReservation(selected.getReservationId());
        if (ok) {
            setStatus("Reservation cancelled.", "status-success");
        } else {
            setStatus("Reservation could not be cancelled.", "status-error");
        }
        refreshAsync();
    }

    @FXML
    private void onConfirm() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ReservationStatus.PENDING) {
            Dialogs.warning("Cannot confirm",
                    "Only PENDING reservations can be confirmed (current: "
                            + selected.getStatus() + ").");
            return;
        }
        ReservationService.setStatus(selected.getReservationId(), ReservationStatus.CONFIRMED);
        setStatus("Reservation confirmed.", "status-success");
        refreshAsync();
    }

    @FXML
    private void onPay() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() == ReservationStatus.CANCELLED
                || selected.getStatus() == ReservationStatus.COMPLETED) {
            Dialogs.warning("Nothing to pay",
                    "Reservation is " + selected.getStatus() + ".");
            return;
        }
        Navigator.goTo(Navigator.CHECKOUT,
                (CheckoutController controller) -> controller.setReservation(selected));
    }

    private void refreshAsync() {
        if (loadService.isRunning()) {
            loadService.cancel();
        }
        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText("Loading reservations...");
        loadService.restart();
    }

    private void applyReservationData(List<Reservation> mine) {
        ObservableList<Reservation> data = FXCollections.observableArrayList(mine);
        table.setItems(data);
        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText(mine.size() + " reservation(s).");
    }

    private void setStatus(String message, String styleClass) {
        statusLabel.getStyleClass().setAll("status-label", styleClass);
        statusLabel.setText(message);
    }

    private static String shorten(String id) {
        return id == null || id.length() < 8 ? id : id.substring(0, 8);
    }

    private final class ReservationLoadService extends Service<List<Reservation>> {
        @Override
        protected Task<List<Reservation>> createTask() {
            Guest guest = Session.getCurrentGuest();
            return new Task<>() {
                @Override
                protected List<Reservation> call() {
                    HotelDatabase.seedDummyData();
                    return HotelDatabase.RESERVATIONS.stream()
                            .filter(r -> r.getGuest() == guest)
                            .toList();
                }
            };
        }
    }
}
