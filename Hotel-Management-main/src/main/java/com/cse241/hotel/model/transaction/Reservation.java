package com.cse241.hotel.model.transaction;

import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.user.Guest;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Reservation {
    private final String reservationId;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private ReservationStatus status;

    public Reservation(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, ReservationStatus status) {
        this(UUID.randomUUID().toString(), guest, room, checkInDate, checkOutDate, status);
    }

    public Reservation(
            String reservationId,
            Guest guest,
            Room room,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            ReservationStatus status
    ) {
        this.reservationId = requireNonBlank(reservationId, "Reservation id is required.");
        this.guest = Objects.requireNonNull(guest, "Guest is required.");
        this.room = Objects.requireNonNull(room, "Room is required.");
        this.checkInDate = Objects.requireNonNull(checkInDate, "Check-in date is required.");
        this.checkOutDate = Objects.requireNonNull(checkOutDate, "Check-out date is required.");
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }
        this.status = Objects.requireNonNull(status, "Reservation status is required.");
    }

    public String getReservationId() {
        return reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = Objects.requireNonNull(status, "Reservation status is required.");
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

