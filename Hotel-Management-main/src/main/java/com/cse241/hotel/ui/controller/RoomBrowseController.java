package com.cse241.hotel.ui.controller;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.exceptions.RoomNotAvailableException;
import com.cse241.hotel.model.property.Amenity;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.services.ReservationService;
import com.cse241.hotel.services.RoomService;
import com.cse241.hotel.ui.Dialogs;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import com.cse241.hotel.ui.concurrent.FxExecutors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class RoomBrowseController implements Initializable {

    private static final String TYPE_ANY = "Any";
    private static final Duration REFRESH_PERIOD = Duration.seconds(3);

    @FXML
    private ChoiceBox<String> typeChoice;
    @FXML
    private TextField maxPriceField;
    @FXML
    private DatePicker checkInPicker;
    @FXML
    private DatePicker checkOutPicker;
    @FXML
    private HBox amenitiesBox;
    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, String> roomNumberCol;
    @FXML
    private TableColumn<Room, String> roomTypeCol;
    @FXML
    private TableColumn<Room, String> capacityCol;
    @FXML
    private TableColumn<Room, String> rateCol;
    @FXML
    private TableColumn<Room, String> amenitiesCol;
    @FXML
    private Label statusLabel;
    @FXML
    private Button reserveButton;

    private final List<CheckBox> amenityCheckBoxes = new ArrayList<>();

    private final ExecutorService refreshExecutor = FxExecutors.newSingleDaemonExecutor("room-refresh");
    private final RoomRefreshService refreshService = new RoomRefreshService();
    private volatile FilterSnapshot currentFilters = FilterSnapshot.defaults();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        populateFilters();

        reserveButton.disableProperty().bind(
                roomsTable.getSelectionModel().selectedItemProperty().isNull()
        );

        LocalDate today = LocalDate.now();
        checkInPicker.setValue(today.plusDays(1));
        checkOutPicker.setValue(today.plusDays(3));

        refreshService.setExecutor(refreshExecutor);
        refreshService.setPeriod(REFRESH_PERIOD);
        refreshService.setRestartOnFailure(true);
        refreshService.setOnSucceeded(evt -> applyRoomDataFromBackground(refreshService.getValue()));
        refreshService.setOnFailed(evt -> {
            Throwable ex = refreshService.getException();
            setError(ex == null ? "Could not refresh rooms." : ex.getMessage());
        });

        snapshotFilters();
        refreshService.start();
    }

    private void configureTable() {
        roomNumberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        roomTypeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType().getName()));
        capacityCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRoomType().getCapacity())));
        rateCol.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().nightlyRate().toPlainString()));
        amenitiesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAmenities().stream()
                        .map(Amenity::getName)
                        .collect(Collectors.joining(", "))
        ));
    }

    private void populateFilters() {
        Set<String> types = new LinkedHashSet<>();
        types.add(TYPE_ANY);
        HotelDatabase.ROOMS.forEach(r -> types.add(r.getRoomType().getName()));
        typeChoice.setItems(FXCollections.observableArrayList(types));
        typeChoice.getSelectionModel().selectFirst();

        Set<String> amenityNames = new LinkedHashSet<>();
        HotelDatabase.ROOMS.forEach(r -> r.getAmenities().forEach(a -> amenityNames.add(a.getName())));
        amenityCheckBoxes.clear();
        amenitiesBox.getChildren().clear();
        for (String amenity : amenityNames) {
            CheckBox cb = new CheckBox(amenity);
            cb.getStyleClass().add("amenity-check");
            amenityCheckBoxes.add(cb);
            amenitiesBox.getChildren().add(cb);
        }
    }

    @FXML
    private void onApplyFilters() {
        snapshotFilters();
        triggerRefreshNow();
    }

    @FXML
    private void onClearFilters() {
        typeChoice.getSelectionModel().selectFirst();
        maxPriceField.clear();
        amenityCheckBoxes.forEach(cb -> cb.setSelected(false));
        snapshotFilters();
        triggerRefreshNow();
    }

    @FXML
    private void onBack() {
        refreshService.cancel();
        refreshExecutor.shutdownNow();
        Navigator.goTo(Navigator.DASHBOARD);
    }

    @FXML
    private void onReserve() {
        Guest guest = Session.getCurrentGuest();
        if (guest == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setError("Please select a room first.");
            return;
        }
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();
        if (checkIn == null || checkOut == null) {
            setError("Please pick both check-in and check-out dates.");
            return;
        }
        try {
            Reservation reservation =
                    ReservationService.createReservation(guest, selected, checkIn, checkOut);
            Dialogs.info(
                    "Reservation created",
                    "Room " + selected.getRoomNumber()
                            + " reserved from " + checkIn + " to " + checkOut
                            + ". Status: " + reservation.getStatus()
            );
            statusLabel.getStyleClass().setAll("status-label", "status-success");
            statusLabel.setText("Reservation #" + shorten(reservation.getReservationId()) + " created.");
        } catch (RoomNotAvailableException ex) {
            Dialogs.warning("Room not available", ex.getMessage());
            setError(ex.getMessage());
        } catch (RuntimeException ex) {
            Dialogs.error("Could not reserve", ex.getMessage());
            setError(ex.getMessage());
        }
    }

    private void snapshotFilters() {
        currentFilters = new FilterSnapshot(
                typeChoice.getValue(),
                parseMaxPrice(maxPriceField.getText()),
                amenityCheckBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    private void triggerRefreshNow() {
        if (refreshService.isRunning()) {
            refreshService.cancel();
        }
        refreshService.restart();
    }

    private void applyRoomDataFromBackground(List<Room> filtered) {
        String selectedRoomNumber = roomsTable.getSelectionModel().getSelectedItem() == null
                ? null
                : roomsTable.getSelectionModel().getSelectedItem().getRoomNumber();

        ObservableList<Room> data = FXCollections.observableArrayList(filtered);
        roomsTable.setItems(data);

        if (selectedRoomNumber != null) {
            roomsTable.getItems().stream()
                    .filter(r -> selectedRoomNumber.equalsIgnoreCase(r.getRoomNumber()))
                    .findFirst()
                    .ifPresent(r -> roomsTable.getSelectionModel().select(r));
        }

        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText(filtered.size() + " room(s) match your filters.");
    }

    private static BigDecimal parseMaxPrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void setError(String message) {
        statusLabel.getStyleClass().setAll("status-label", "status-error");
        statusLabel.setText(message);
    }

    private static String shorten(String id) {
        return id == null || id.length() < 8 ? id : id.substring(0, 8);
    }

    private record FilterSnapshot(String type, BigDecimal maxPrice, Set<String> requiredAmenities) {
        static FilterSnapshot defaults() {
            return new FilterSnapshot(TYPE_ANY, null, Set.of());
        }
    }

    private final class RoomRefreshService extends ScheduledService<List<Room>> {
        @Override
        protected Task<List<Room>> createTask() {
            FilterSnapshot snapshot = currentFilters;
            return new Task<>() {
                @Override
                protected List<Room> call() {
                    List<Room> all = RoomService.searchRooms("");
                    return all.stream()
                            .filter(r -> snapshot.type == null || TYPE_ANY.equals(snapshot.type)
                                    || r.getRoomType().getName().equalsIgnoreCase(snapshot.type))
                            .filter(r -> snapshot.maxPrice == null || r.nightlyRate().compareTo(snapshot.maxPrice) <= 0)
                            .filter(r -> snapshot.requiredAmenities.isEmpty()
                                    || r.getAmenities().stream().map(Amenity::getName).collect(Collectors.toSet())
                                            .containsAll(snapshot.requiredAmenities))
                            .toList();
                }
            };
        }
    }
}
