package com.cse241.hotel.db;

import com.cse241.hotel.enums.Gender;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.model.property.Amenity;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.property.RoomType;
import com.cse241.hotel.model.transaction.Invoice;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Admin;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.model.user.Receptionist;
import com.cse241.hotel.model.user.Staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HotelDatabase {
    public static final ArrayList<Guest> GUESTS = new ArrayList<>();
    public static final ArrayList<Staff> STAFF = new ArrayList<>();
    public static final ArrayList<Room> ROOMS = new ArrayList<>();
    public static final ArrayList<Reservation> RESERVATIONS = new ArrayList<>();
    public static final ArrayList<Invoice> INVOICES = new ArrayList<>();

    private static boolean SEEDED = false;

    private HotelDatabase() {
    }

    public static synchronized void seedDummyData() {
        if (SEEDED) {
            return;
        }
        SEEDED = true;

        STAFF.add(new Admin("Sara", "Admin@2026", "09:00 - 17:00"));
        STAFF.add(new Receptionist("Ellie", "Ellie@2026", "08:00 - 16:00"));

        RoomType single = new RoomType("Single", new BigDecimal("80.00"), 1);
        RoomType doubleRoom = new RoomType("Double", new BigDecimal("120.00"), 2);
        RoomType suite = new RoomType("Suite", new BigDecimal("220.00"), 4);

        Amenity wifi = new Amenity("WiFi", BigDecimal.ZERO);
        Amenity tv = new Amenity("TV", new BigDecimal("5.00"));
        Amenity minibar = new Amenity("Mini-bar", new BigDecimal("15.00"));

        ROOMS.add(new Room("101", single, List.of(wifi, tv)));
        ROOMS.add(new Room("102", single, List.of(wifi)));
        ROOMS.add(new Room("201", doubleRoom, List.of(wifi, tv)));
        ROOMS.add(new Room("301", suite, List.of(wifi, tv, minibar)));

        Guest g1 = new Guest(
                "Ahmed",
                "Password1",
                LocalDate.now().minusYears(25),
                new BigDecimal("500.00"),
                "Downtown Street 1",
                Gender.MALE,
                List.of("Suite", "High floor")
        );
        Guest g2 = new Guest(
                "bob",
                "bob@2026",
                LocalDate.now().minusYears(30),
                new BigDecimal("50.00"),
                "Main Road 5",
                Gender.MALE,
                List.of("Single")
        );
        GUESTS.add(g1);
        GUESTS.add(g2);

        Reservation r1 = new Reservation(
                g1,
                ROOMS.get(0),
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(6),
                ReservationStatus.CONFIRMED
        );
        Reservation r2 = new Reservation(
                g2,
                ROOMS.get(1),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                ReservationStatus.PENDING
        );
        RESERVATIONS.add(r1);
        RESERVATIONS.add(r2);
    }

    public static synchronized void resetForTests() {
        GUESTS.clear();
        STAFF.clear();
        ROOMS.clear();
        RESERVATIONS.clear();
        INVOICES.clear();
        SEEDED = false;
        seedDummyData();
    }

    public static Optional<Guest> findGuestByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return GUESTS.stream()
                .filter(g -> g.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public static Optional<Staff> findStaffByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return STAFF.stream()
                .filter(s -> s.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public static Optional<Room> findRoomByNumber(String roomNumber) {
        if (roomNumber == null) {
            return Optional.empty();
        }
        return ROOMS.stream()
                .filter(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst();
    }

    public static Optional<Reservation> findReservationById(String reservationId) {
        if (reservationId == null) {
            return Optional.empty();
        }
        return RESERVATIONS.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst();
    }

    public static Optional<Invoice> findInvoiceById(String invoiceId) {
        if (invoiceId == null) {
            return Optional.empty();
        }
        return INVOICES.stream()
                .filter(i -> i.getInvoiceId().equals(invoiceId))
                .findFirst();
    }

    public static void requireUniqueGuestUsername(String username) {
        Objects.requireNonNull(username, "Username is required.");
        if (findGuestByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    public static void requireUniqueStaffUsername(String username) {
        Objects.requireNonNull(username, "Username is required.");
        if (findStaffByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Staff username already exists: " + username);
        }
    }

    public static void requireUniqueRoomNumber(String roomNumber) {
        Objects.requireNonNull(roomNumber, "Room number is required.");
        if (findRoomByNumber(roomNumber).isPresent()) {
            throw new IllegalArgumentException("Room number already exists: " + roomNumber);
        }
    }
}

