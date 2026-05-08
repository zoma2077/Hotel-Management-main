package com.cse241.hotel.ui.controller;

import com.cse241.hotel.model.property.Amenity;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.property.RoomType;
import com.cse241.hotel.model.user.Staff;
import com.cse241.hotel.services.RoomService;
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
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class AdminRoomsController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, String> numberCol;
    @FXML
    private TableColumn<Room, String> typeCol;
    @FXML
    private TableColumn<Room, String> capacityCol;
    @FXML
    private TableColumn<Room, String> nightlyCol;
    @FXML
    private TableColumn<Room, String> amenitiesCol;

    @FXML
    private TextField roomNumberField;
    @FXML
    private TextField typeNameField;
    @FXML
    private TextField basePriceField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField amenitiesField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private final ExecutorService loadExecutor = FxExecutors.newSingleDaemonExecutor("admin-rooms-load");
    private final RoomsLoadService loadService = new RoomsLoadService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            Navigator.goTo(Navigator.LOGIN);
            return;
        }

        configureTable();

        loadService.setExecutor(loadExecutor);
        loadService.setOnSucceeded(evt -> applyData(loadService.getValue()));
        loadService.setOnFailed(evt -> {
            Throwable ex = loadService.getException();
            setStatus(ex == null ? "Failed to load rooms." : ex.getMessage(), "status-error");
        });

        roomsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            roomNumberField.setText(newV.getRoomNumber());
            typeNameField.setText(newV.getRoomType().getName());
            basePriceField.setText(newV.getRoomType().getBasePricePerNight().toPlainString());
            capacityField.setText(String.valueOf(newV.getRoomType().getCapacity()));
            amenitiesField.setText(newV.getAmenities().stream().map(Amenity::getName).collect(Collectors.joining(", ")));
        });

        updateButton.disableProperty().bind(roomsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(roomsTable.getSelectionModel().selectedItemProperty().isNull());

        refreshAsync();
    }

    private void configureTable() {
        numberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType().getName()));
        capacityCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRoomType().getCapacity())));
        nightlyCol.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().nightlyRate().toPlainString()));
        amenitiesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAmenities().stream().map(Amenity::getName).collect(Collectors.joining(", "))
        ));
    }

    @FXML
    private void onRefresh() {
        refreshAsync();
    }

    @FXML
    private void onAdd() {
        try {
            Room room = buildRoomFromFields();
            RoomService.addRoom(room);
            setStatus("Room " + room.getRoomNumber() + " added.", "status-success");
            clearForm();
            refreshAsync();
        } catch (RuntimeException ex) {
            Dialogs.error("Could not add room", ex.getMessage());
            setStatus(ex.getMessage(), "status-error");
        }
    }

    @FXML
    private void onUpdate() {
        try {
            Room room = buildRoomFromFields();
            RoomService.updateRoom(room);
            setStatus("Room " + room.getRoomNumber() + " updated.", "status-success");
            refreshAsync();
        } catch (RuntimeException ex) {
            Dialogs.error("Could not update room", ex.getMessage());
            setStatus(ex.getMessage(), "status-error");
        }
    }

    @FXML
    private void onDelete() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!Dialogs.confirm("Delete room?", "Delete room " + selected.getRoomNumber() + "?")) {
            return;
        }
        try {
            boolean ok = RoomService.deleteRoomByNumber(selected.getRoomNumber());
            if (ok) {
                setStatus("Room deleted.", "status-success");
                clearForm();
                refreshAsync();
            } else {
                setStatus("Room could not be deleted.", "status-error");
            }
        } catch (RuntimeException ex) {
            Dialogs.error("Could not delete room", ex.getMessage());
            setStatus(ex.getMessage(), "status-error");
        }
    }

    @FXML
    private void onBack() {
        loadService.cancel();
        loadExecutor.shutdownNow();
        Navigator.goTo(Navigator.STAFF_DASHBOARD);
    }

    @FXML
    private void onChat() {
        Navigator.goTo(Navigator.CHAT);
    }

    private void refreshAsync() {
        if (loadService.isRunning()) {
            loadService.cancel();
        }
        setStatus("Loading rooms...", "status-label");
        loadService.restart();
    }

    private void applyData(List<Room> rooms) {
        ObservableList<Room> data = FXCollections.observableArrayList(rooms);
        roomsTable.setItems(data);
        statusLabel.getStyleClass().setAll("status-label");
        statusLabel.setText(rooms.size() + " room(s).");
    }

    private Room buildRoomFromFields() {
        String number = roomNumberField.getText();
        String typeName = typeNameField.getText();
        BigDecimal basePrice = parseMoney(basePriceField.getText(), "Base price is required.");
        int capacity = parseInt(capacityField.getText(), "Capacity is required.");
        List<Amenity> amenities = parseAmenities(amenitiesField.getText());
        RoomType type = new RoomType(typeName, basePrice, capacity);
        return new Room(number, type, amenities);
    }

    private static BigDecimal parseMoney(String raw, String messageIfMissing) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(messageIfMissing);
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid money amount: " + raw);
        }
    }

    private static int parseInt(String raw, String messageIfMissing) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(messageIfMissing);
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: " + raw);
        }
    }

    private static List<Amenity> parseAmenities(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<String> names = List.of(raw.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return names.stream()
                .map(n -> new Amenity(n, BigDecimal.ZERO))
                .toList();
    }

    private void clearForm() {
        roomNumberField.clear();
        typeNameField.clear();
        basePriceField.clear();
        capacityField.clear();
        amenitiesField.clear();
        roomsTable.getSelectionModel().clearSelection();
    }

    private void setStatus(String message, String styleClass) {
        statusLabel.getStyleClass().setAll("status-label", styleClass);
        statusLabel.setText(message);
    }

    private final class RoomsLoadService extends Service<List<Room>> {
        @Override
        protected Task<List<Room>> createTask() {
            String query = searchField == null ? "" : searchField.getText();
            return new Task<>() {
                @Override
                protected List<Room> call() {
                    return RoomService.searchRooms(query);
                }
            };
        }
    }
}

