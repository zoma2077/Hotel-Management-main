package com.cse241.hotel.ui.controller;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Staff;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ReceptionistReservationsController implements Initializable {

    @FXML
    private TextField guestUsernameField;
    @FXML
    private TextField roomNumberField;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private ChoiceBox<ReservationStatus> statusChoice;

    @FXML
    private TableView<Reservation> table;
    @FXML
    private TableColumn<Reservation, String> idCol;
    @FXML
    private TableColumn<Reservation, String> guestCol;
    @FXML
    private TableColumn<Reservation, String> roomCol;
    @FXML
    private TableColumn<Reservation, String> datesCol;
    @FXML
    private TableColumn<Reservation, String> statusCol;

    @FXML
    private Label statusLabel;
    @FXML
    private Button checkInButton;
    @FXML
    private Button checkOutButton;
    @FXML
    private Button payButton;

    private final ExecutorService loadExecutor = FxExecutors.newSingleDaemonExecutor("reception-res-load");
    private final ReservationSearchService searchService = new ReservationSearchService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }

        configureTable();
        statusChoice.setItems(FXCollections.observableArrayList(ReservationStatus.values()));

        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today.minusDays(1));
        toDatePicker.setValue(today.plusDays(30));

        searchService.setExecutor(loadExecutor);
        searchService.setOnSucceeded(evt -> applyData(searchService.getValue()));
        searchService.setOnFailed(evt -> {
            Throwable ex = searchService.getException();
            setStatus(ex == null ? "Failed to search reservations." : ex.getMessage(), "status-error");
        });

        checkInButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        checkOutButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        payButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        refreshAsync();
    }

    private void configureTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(shorten(c.getValue().getReservationId())));
        guestCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGuest().getUsername()));
        roomCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom().getRoomNumber()
                + " (" + c.getValue().getRoom().getRoomType().getName() + ")"));
        datesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckInDate() + " → " + c.getValue().getCheckOutDate()
        ));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
    }

    @FXML
    private void onSearch() {
        refreshAsync();
    }

    @FXML
    private void onClear() {
        guestUsernameField.clear();
        roomNumberField.clear();
        statusChoice.setValue(null);
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today.minusDays(1));
        toDatePicker.setValue(today.plusDays(30));
        refreshAsync();
    }

    @FXML
    private void onCheckIn() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ReservationStatus.PENDING) {
            Dialogs.warning("Cannot check-in", "Only PENDING reservations can be checked-in.");
            return;
        }
        ReservationService.setStatus(selected.getReservationId(), ReservationStatus.CONFIRMED);
        setStatus("Checked-in: reservation confirmed.", "status-success");
        refreshAsync();
    }

    @FXML
    private void onCheckOut() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ReservationStatus.CONFIRMED) {
            Dialogs.warning("Cannot check-out", "Only CONFIRMED reservations can be checked-out.");
            return;
        }
        ReservationService.setStatus(selected.getReservationId(), ReservationStatus.COMPLETED);
        setStatus("Checked-out: reservation completed.", "status-success");
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
            Dialogs.warning("Nothing to pay", "Reservation is " + selected.getStatus() + ".");
            return;
        }
        Navigator.goTo(Navigator.CHECKOUT,
                (CheckoutController controller) -> controller.setReservation(selected));
    }

    @FXML
    private void onChat() {
        Navigator.goTo(Navigator.CHAT);
    }

    @FXML
    private void onBack() {
        searchService.cancel();
        loadExecutor.shutdownNow();
        Navigator.goTo(Navigator.STAFF_DASHBOARD);
    }

    private void refreshAsync() {
        if (searchService.isRunning()) {
            searchService.cancel();
        }
        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText("Searching...");
        searchService.restart();
    }

    private void applyData(List<Reservation> reservations) {
        ObservableList<Reservation> data = FXCollections.observableArrayList(reservations);
        table.setItems(data);
        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText(reservations.size() + " reservation(s) match.");
    }

    private void setStatus(String message, String styleClass) {
        statusLabel.getStyleClass().setAll("status-label", styleClass);
        statusLabel.setText(message);
    }

    private FilterSnapshot snapshotFilters() {
        return new FilterSnapshot(
                guestUsernameField.getText(),
                roomNumberField.getText(),
                fromDatePicker.getValue(),
                toDatePicker.getValue(),
                statusChoice.getValue()
        );
    }

    private static boolean matchesDateRange(Reservation r, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        LocalDate start = from == null ? LocalDate.MIN : from;
        LocalDate end = to == null ? LocalDate.MAX : to;
        return ReservationService.rangesOverlap(start, end.plusDays(1), r.getCheckInDate(), r.getCheckOutDate());
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String shorten(String id) {
        return id == null || id.length() < 8 ? id : id.substring(0, 8);
    }

    private record FilterSnapshot(
            String guestUsername,
            String roomNumber,
            LocalDate fromDate,
            LocalDate toDate,
            ReservationStatus status
    ) {
    }

    private final class ReservationSearchService extends Service<List<Reservation>> {
        @Override
        protected Task<List<Reservation>> createTask() {
            FilterSnapshot snapshot = snapshotFilters();
            return new Task<>() {
                @Override
                protected List<Reservation> call() {
                    HotelDatabase.seedDummyData();

                    String guestQ = normalize(snapshot.guestUsername);
                    String roomQ = normalize(snapshot.roomNumber);
                    ReservationStatus status = snapshot.status;

                    return HotelDatabase.RESERVATIONS.stream()
                            .filter(r -> guestQ.isEmpty() || normalize(r.getGuest().getUsername()).contains(guestQ))
                            .filter(r -> roomQ.isEmpty() || normalize(r.getRoom().getRoomNumber()).contains(roomQ))
                            .filter(r -> status == null || r.getStatus() == status)
                            .filter(r -> matchesDateRange(r, snapshot.fromDate, snapshot.toDate))
                            .toList();
                }
            };
        }
    }
}

